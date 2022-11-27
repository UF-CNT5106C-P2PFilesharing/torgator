package Process;
/*
 *                     CNT5106C Project
 * This is the program starting remote processes.
 * This program was only tested on CISE SunOS environment.
 * If you use another environment, for example, linux environment in CISE
 * or other environments not in CISE, it is not guaranteed to work properly.
 * It is your responsibility to adapt this program to your running environment.
 */

import java.io.ByteArrayOutputStream;
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

            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter username: ");
            String username = scanner.next();

            System.out.println();
            System.out.print("Enter password: ");
            String password = scanner.next();

            // start clients at remote hosts
            System.out.println(path);
            for (int i = 0; i < myStart.peerInfoVector.size(); i++) {
                PeerMetadata pInfo = myStart.peerInfoVector.elementAt(i);
                session = new JSch().getSession(username,  pInfo.getHostAddress() , 22);
                session.setPassword(password);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();

                channel = (ChannelExec) session.openChannel("exec");
                channel.setCommand("cd " + path + "; java Process/peerProcess " + pInfo.getId());
                ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
                channel.setOutputStream(responseStream);
                channel.connect();
//                System.out.println("Start remote peer " + pInfo.getId() +  " at " + pInfo.getHostAddress());
//                String startSeederLeecher = "cd " + path + "; java Process/peerProcess " + pInfo.getId();
//                Runtime.getRuntime().exec("ssh " + username + "@" + pInfo.getHostAddress() + startSeederLeecher);
//                Runtime.getRuntime().exec("java Process/peerProcess " + pInfo.getId());
                TimeUnit.SECONDS.sleep(3);
            }
            System.out.println("Starting all remote peers has done." );

        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
