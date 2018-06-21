package Ping;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Screen {
    private JTextField textField1;
    private JButton checkPingButton;
    private JTextArea textArea1;
    private JPanel Panel;
    private JTextField textField2;
    private JButton saveButton;
    private JScrollPane scroll;
    private JProgressBar progressBar1;
    private String OS = System.getProperty("os.name");
    private List<String> commands = new ArrayList<String>();
    private List<String> fullText = new ArrayList<String>();

    public Screen() {
        checkPingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    init();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    save();
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } catch (UnsupportedEncodingException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }
    public void save() throws FileNotFoundException, UnsupportedEncodingException {
        new Thread(() -> {
            Date dNow = new Date();
            SimpleDateFormat ft = new SimpleDateFormat("dd-MM-yyyy_'at'_HH.mm");
            String name = ft.format(dNow) + ".txt"; //20-06-2018_at_19.10.txt
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(name, "UTF-8");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            for (String line : fullText) {
                writer.println(line);
            }
            writer.flush();
            writer.close();
        }).start();
    }
    public void init() throws IOException {
        fullText.clear();
        textArea1.setText("");
        new Thread(() -> {
            commands.clear();
            commands.add("ping");
            commands.add(textField1.getText());
            if(OS.contains("Windows"))
                commands.add("-n");
            else
                commands.add("-c");
            commands.add(textField2.getText());
            try {
                doCommand(commands);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    public static void main(String[] args) throws IOException {
        JFrame frame = new JFrame("Ping Tester v1.3 by : António Alexandre");
        frame.setContentPane(new Screen().Panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
    public void doCommand(List<String> command)
            throws IOException {
        checkPingButton.setEnabled(false);
        saveButton.setEnabled(false);
        String s = null;
        ProcessBuilder pb = new ProcessBuilder(command);
        Process process = pb.start();

        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

        int num = 1, sum = 0, err = 0, max = 0;
        JScrollBar scrollV = scroll.getVerticalScrollBar();
        // read the output from the command
        while ((s = stdInput.readLine()) != null)
        {
            fullText.add(s);
            int t = s.indexOf("time");
            int lim = t;
            if(OS.contains("Windows"))
                lim = s.indexOf("ms");
            else
                lim = s.lastIndexOf(".");
            if(!s.equals("Request timed out.") && t != -1 && lim != -1) {
                changeText("Test " + num + ": ");
                String valStr = "";
                for (int i = t+5; i < lim; i++) {
                    valStr += s.charAt(i);
                    changeText(s.charAt(i));
                }
                int valInt = Integer.parseInt(valStr);
                if(valInt > max)
                    max = valInt;
                sum += valInt;
                changeText("ms\n");
                num++;
            }
            else if (s.contains("Request")){
                changeText(s + "\n");
                num++;
                err++;
            }
            else if (s.contains("Bad value for option -n") || s.contains("invalid count of packets to transmit")){
                changeText(s + "\n");
                num++;
            }
            else if (s.contains("General failure")){
                changeText(s + "\n");
                num++;
                break;
            }
            scrollV.setValue( scrollV.getMaximum() + 1);
        }
        if(err < num-1) {
            int average = sum / (num - 1 - err);
            changeText("Average Ping: " + average + "ms\n");
            changeText("Max Latency: " + max + "ms\n");
            scrollV.setValue(scrollV.getMaximum() + 1);
            progressBar1.setMaximum(100);
            if (err > 0 || max > 400) {
                progressBar1.setForeground(Color.RED);
                progressBar1.setValue(10);
            } else if (average < 40) {
                progressBar1.setForeground(Color.GREEN);
                progressBar1.setValue(100);
            } else if (average > 150) {
                progressBar1.setForeground(Color.YELLOW);
                progressBar1.setValue(15);
            } else {
                progressBar1.setForeground(Color.GREEN);
                progressBar1.setValue((int)(20+(80*((float)40/(float)average))));
            }
        }
        else{
            changeText("Can't connect to the server, all packets lost!\n");
            progressBar1.setForeground(Color.RED);
            progressBar1.setValue(100);
        }
        checkPingButton.setEnabled(true);
        if(num > 2)
            saveButton.setEnabled(true);
    }

    public void changeText(char s){
        String str = "" + s;
        this.textArea1.append(str);
    }
    public void changeText(String s){
        this.textArea1.append(s);
    }
}
