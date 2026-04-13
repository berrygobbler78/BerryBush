package com.berrygobbler78.flacplayer.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jflac.FLACDecoder;
import org.jflac.PCMProcessor;
import org.jflac.metadata.StreamInfo;
import org.jflac.util.ByteData;
import org.jflac.util.WavWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

public class FlacDecoder implements PCMProcessor {
    private final Logger logger = LogManager.getLogger();

    private WavWriter wav;

    public Optional<Path> flacToWav(String source) throws IOException {
        var tempFile =
                File.createTempFile("temp", ".wav", ResourceHandler.get(ResourceHandler.ResourceType.TEMP));

        try (var is = new FileInputStream(source); var os = new FileOutputStream(tempFile.getPath())) {
            wav = new WavWriter(os);

            var flacDecoder = new FLACDecoder(is);
            flacDecoder.addPCMProcessor(this);
            flacDecoder.decode();

            return Optional.of(tempFile.toPath());
        } catch (IOException e) {
            logger.error("Error while converting '{}' | {}", source, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void processStreamInfo(StreamInfo info) {
        try {
            wav.writeHeader(info);
        } catch (IOException e) {
            logger.error("Error while processing '{}' : {}", info, e.getMessage());
        }
    }

    @Override
    public void processPCM(ByteData pcm) {
        try {
            wav.writePCM(pcm);
        } catch (IOException e) {
            logger.error("Error while processing PCM : {}", e.getMessage());
        }
    }
}
