package Process;
/*
 *                     CNT5106C Project
 * This is the program starting remote processes.
 * This program was only tested on CISE SunOS environment.
 * If you use another environment, for example, linux environment in CISE
 * or other environments not in CISE, it is not guaranteed to work properly.
 * It is your responsibility to adapt this program to your running environment.
 */

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import Metadata.PeerMetadata;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import static Configurations.SystemConfiguration.peerInfoFile;

/*
 * The StartRemotePeers class begins remote peer processes.
 * It reads configuration file PeerInfo.cfg and starts remote peer processes.
 * You must modify this program a little bit if your peer processes are written in C or C++.
 * Please look at the lines below the comment saying IMPORTANT.
 */
public class StartRemotePeers {

    public Vector<PeerMetadata> peerInfoVector;
    public static String path = System.getProperty("user.dir");

    public void getConfiguration()
    {
        peerInfoVector = new Vector<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(peerInfoFile));
            for (int i = 0; i < lines.size(); i++) {
                String[] properties = lines.get(i).split("\\s+");
                peerInfoVector.addElement(new PeerMetadata(properties[0], properties[1], properties[2], Integer.parseInt(properties[3]), i));
            }
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        try {
            StartRemotePeers myStart = new StartRemotePeers();
            myStart.getConfiguration();
            Session session;
            ChannelExec channel;

//            Scanner scanner = new Scanner(System.in);
//            System.out.println("Enter the username :");
//            String username = scanner.next();
//            System.out.println("Enter the password :");
//            String password = scanner.next();

            // start clients at remote hosts
            System.out.println(path);
            for (int i = 0; i < myStart.peerInfoVector.size(); i++) {
                PeerMetadata pInfo = myStart.peerInfoVector.elementAt(i);

                System.out.println("Start remote peer " + pInfo.getId() +  " at " + pInfo.getHostAddress());
//                session = new JSch().getSession(username, pInfo.getHostAddress(), 22);
//                session.setPassword(password);
//                session.setConfig("StrictHostKeyChecking", "no");
//                session.connect();

//                channel = (ChannelExec) session.openChannel("exec");
                String buildCommand = "cd " + path + "; make peerProcess.class";
                String startSeederLeecher = "cd " + path + "; java peerProcess " + pInfo.getId();
                if( i == 0) {
//                    channel.setCommand(buildCommand);
//                    ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
//                    channel.setOutputStream(responseStream);
//                    channel.connect();
                    Runtime.getRuntime().exec(buildCommand);
                    TimeUnit.SECONDS.sleep(3);
                }
//                channel.setCommand(startSeederLeecher);
//                ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
//                channel.setOutputStream(responseStream);
//                channel.connect();
                Runtime.getRuntime().exec(startSeederLeecher);
                TimeUnit.SECONDS.sleep(3);
            }
            System.out.println("Starting all remote peers has done." );

        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
