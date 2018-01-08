package io.github.jianzhichun.danmu.analyzer;

import java.nio.ByteOrder;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.rx.RxResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.google.common.collect.ImmutableMap;
import com.hankcs.hanlp.HanLP;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.reactivex.netty.client.ConnectionRequest;
import io.reactivex.netty.protocol.tcp.client.TcpClient;
import rx.Observable;

@SpringBootApplication
public class DanmuAnalyzerApplication extends WebMvcConfigurerAdapter {

	public static void main(String[] args) {
		SpringApplication.run(DanmuAnalyzerApplication.class, args);
	}
	
	
	
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addRedirectViewController("/", "/view");
		registry.addViewController("/view/**").setViewName("forward:/index.html");
	}


	@Bean ConnectionRequest<ByteBuf, String> douyuConnectionRequest(){
		final Logger LOG = LoggerFactory.getLogger("douyuConnectionRequest");
		return TcpClient
				.newClient("openbarrage.douyutv.com", 8601)
				.addChannelHandlerLast("message2byte-encoder",
					() -> new MessageToByteEncoder<String>() {
						/*
		                        =================================================
		                        |   Byte0   |   Byte1   |   Byte2   |   Byte3   |
		                        =================================================
		                        |                    消息长度                     |
		                        =================================================
		                        |                    消息长度                     |
		                 Header -------------------------------------------------
		                        |         消息类型        |  加密字段  |  保留字段   |
		                        =================================================
		                 Body   |                数据部分('\0'结尾)                |
		                        =================================================
		               */
						protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) throws Exception {
							LOG.info("Send: {} to douyu", msg);
	                        out.writeIntLE(msg.getBytes().length + 9)
	                        	.writeIntLE(msg.getBytes().length + 9)
	                        	.writeShortLE(689)
	                        	.writeShortLE(0)
	                        	.writeBytes(msg.getBytes())
	                        	.writeByte(0);
	                    }
					})
				.addChannelHandlerLast("length-field-based-frame-decoder",
					() -> new LengthFieldBasedFrameDecoder(ByteOrder.LITTLE_ENDIAN, 2 << 15, 0, 4, 0, 12, false))
				.addChannelHandlerLast("delimiter-based-frame-decoder", 
					() -> new DelimiterBasedFrameDecoder(2 << 15, Unpooled.copiedBuffer("\0".getBytes())))
				.<ByteBuf, String>addChannelHandlerLast("string-decoder", StringDecoder::new)
				.createConnectionRequest();
	}
	
	@RestController
	@RequestMapping("/api")
	class Danmu {
		
		private final Pattern STT_PATTERN = Pattern.compile("(nn@=)(?<nn>(.+?))\\/(txt@=)(?<txt>(.+?))\\/");

		@Autowired ConnectionRequest<ByteBuf, String> douyuConnectionRequest;
		
		@GetMapping("/{platform}-{roomid}.stream")
		public SseEmitter stream(@PathVariable String platform, @PathVariable Integer roomid,
				@RequestParam(name="bufferTime", defaultValue="30") Long bufferTime){
			switch(platform){
			case "douyu": 
				return RxResponse.sse(douyuConnectionRequest
					.flatMap(conn -> 
						Observable.merge(
							conn
								.writeString(Observable.just(
									String.format("type@=loginreq/roomid@=%d/", roomid),
									String.format("type@=joingroup/rid@=%d/gid@=-9999/", roomid)))
								.cast(String.class), 
							//Heartbeat
							conn
								.writeString(Observable
									.interval(30, TimeUnit.SECONDS)
									.map(i->"type@=mrkl/"))
								.cast(String.class), 
							conn.getInput()))
					.map(line -> {
						Matcher matcher = STT_PATTERN.matcher(line);
						if(matcher.find()){
							String nn = matcher.group("nn");
							String txt = matcher.group("txt").replaceAll("@A", "@").replaceAll("@S", "/");
							return ImmutableMap.builder()
									.put("nn", nn)
									.put("tok", HanLP.segment(txt)
											.stream().map(term->term.word).toArray())
									.put("kw", HanLP.extractKeyword(txt, 3))
									.build();
						}
						return null;
					})
					.filter(Objects::nonNull)
					.buffer(bufferTime > 30 ? bufferTime : 30, TimeUnit.SECONDS));
			default: return null;
			}
			
		}
		
	}
	
}
