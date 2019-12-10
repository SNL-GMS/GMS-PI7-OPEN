package gms.processors.dataacquisition.transferredfilecreator;

import com.google.common.primitives.Longs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SequenceNumberUtility {


    public static synchronized long getAndUpdateSequenceNumber(String seqNumFileName) throws IOException {
        //See if the sequence number file exits
        File seqNumFile = new File(seqNumFileName);
        if(!seqNumFile.exists()){
            seqNumFile.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(seqNumFileName));
            writer.write("0");
            writer.close();
        }

        Path seqNumFilePath = Paths.get(seqNumFile.toURI());
        //Get the sequence number, this was the last one used so the thread using this will want to increment by 1
        String seqNumString = new String(Files.readAllBytes(seqNumFilePath));
        long newSeqNum = Long.parseLong(seqNumString) + 1;
        //now write it to the file
        BufferedWriter writer = new BufferedWriter(new FileWriter(seqNumFileName));
        writer.write(Long.toString(newSeqNum));
        writer.close();
        //return the sequence number for the thread to use
        return newSeqNum;
    }
}
