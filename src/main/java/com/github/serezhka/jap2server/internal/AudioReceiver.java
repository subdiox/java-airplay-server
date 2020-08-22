package com.github.serezhka.jap2server.internal;

import com.github.serezhka.jap2server.internal.handler.audio.AudioHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class AudioReceiver implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(AudioReceiver.class);

    private final AudioHandler audioHandler;

    public AudioReceiver(AudioHandler audioHandler) {
        this.audioHandler = audioHandler;
    }

    @Override
    public void run() {
        var port = 4998;
        var bootstrap = new Bootstrap();
        var workerGroup = eventLoopGroup();

        try {
            bootstrap
                    .group(workerGroup)
                    .channel(datagramChannelClass())
                    .localAddress(new InetSocketAddress(port))
                    .handler(new ChannelInitializer<DatagramChannel>() {
                        @Override
                        public void initChannel(final DatagramChannel ch) {
                            ch.pipeline().addLast(audioHandler);
                        }
                    });
            var channelFuture = bootstrap.bind().sync();
            log.info("Audio receiver listening on port: {}", port);
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.info("Audio receiver interrupted");
        } finally {
            log.info("Audio receiver stopped");
            workerGroup.shutdownGracefully();
        }
    }

    private EventLoopGroup eventLoopGroup() {
        return new NioEventLoopGroup();
    }

    private Class<? extends DatagramChannel> datagramChannelClass() {
        return NioDatagramChannel.class;
    }
}
