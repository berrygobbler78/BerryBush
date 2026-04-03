package com.berrygobbler78.flacplayer.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jflac.FLACDecoder;
import org.jflac.PCMProcessor;
import org.jflac.metadata.StreamInfo;
import org.jflac.util.ByteData;
import org.jflac.util.WavWriter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FlacDecoder implements PCMProcessor {
    private final Logger logger = LogManager.getLogger();

    private WavWriter wav;

    public void flacToWav(String inPath, String outPath) {
        try (FileInputStream is = new FileInputStream(inPath); FileOutputStream os = new FileOutputStream(outPath)) {
            wav = new WavWriter(os);
            FLACDecoder flacDecoder = new FLACDecoder(is);
            flacDecoder.addPCMProcessor(this);
            flacDecoder.decode();
        } catch (IOException e) {
            logger.error("Error while converting '{}' : {}", inPath, e.getMessage());
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
