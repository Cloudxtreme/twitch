import java.net.*;
import java.io.*;
import java.util.*;
import javax.sound.sampled.*;

import org.json.*;

public class Main extends TimerTask implements LineListener {

    private static String channelName = "vlavolk";
    private static long waitTime = 0L;

    private static void printUsage() {
        System.out.println("java -jar tsc.jar -T 1 (in minutes) -U test_channel");
        System.exit(1);
    }
    
    public static void main(String[] args) throws Exception{
        int argsLen = args.length;
        if (argsLen != 4)
            printUsage();
        for (int i = 0; i < argsLen; i+=2) {
            if (args[i].charAt(0) != '-')
                printUsage();
            switch(args[i].charAt(1)) {
                case 'U':
                    channelName = args[i+1];
                break;
                case 'T':
                    waitTime = (long)(Float.parseFloat(args[i+1])*60000.0f);
                break;
                default:
                    printUsage();
            }
        }
        Main m = new Main();
        Timer timer = new Timer();
        timer.schedule(m, new Date(), waitTime);
    }
    
    public void run() {
        URL channelURL = null;
        try {
            channelURL = new URL("https://api.twitch.tv/kraken/channels/" + Main.channelName);
        } catch (MalformedURLException mue) {
        }
        try {
            URLConnection conn = channelURL.openConnection();
            conn.addRequestProperty("Accept", "application/vnd.twitchtv.v3+json");
            conn.connect();
            InputStream is = conn.getInputStream();
            StringBuilder data = new StringBuilder();
            Scanner scan = new Scanner(is);
            while (scan.hasNext())
                data.append(scan.nextLine());
            scan.close();
            JSONObject obj = new JSONObject(data.toString());
            boolean isOnline = false;
            try {
                String status = obj.getString("status");
                isOnline = true;
            } catch (Exception e) {
                isOnline = false;
            }
            if (isOnline) {
                System.out.print(obj.getString("status") + "\n" + obj.getString("game") + "\n");
                playSound("siren.wav");
            } else {
                System.out.println("Channel is offline");
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
    
    @Override
    public void update(LineEvent event) {
    
    }
    
    public static synchronized void playSound(final String name) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(Main.class.getResourceAsStream("/" + name)));
                    AudioFormat format = ais.getFormat();
                    DataLine.Info info = new DataLine.Info(Clip.class, format);
                    Clip clip = (Clip) AudioSystem.getLine(info);
                    clip.open(ais);
                    clip.start();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }).start();
    }
}