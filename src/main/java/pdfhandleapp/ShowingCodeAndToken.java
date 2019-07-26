package pdfhandleapp;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.google.api.services.gmail.model.Label;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import javax.swing.text.BadLocationException;
import mygmailapi.TokenNotifications;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;

public class ShowingCodeAndToken extends JFrame {

    private static TokenNotifications gmail;
    private static boolean beOn = true;
    private static ShowingCodeAndToken showing;
    private static String contraseñaPdf = "";
//9mfpxzf7

    public boolean show(byte[] pdfData) {

//        Security.addProvider( new BouncyCastleProvider() );
        PDDocument document = null;
        try {
            document = PDDocument.load(pdfData, contraseñaPdf);
        } catch (IOException ex) {
            contraseñaPdf = JOptionPane.showInputDialog(this, "Contraseña incorrecta reinténtalo!", "Error!!!!", JOptionPane.ERROR_MESSAGE);
            if (contraseñaPdf != null && !contraseñaPdf.equals("")) {
                show(pdfData);
            }
            Logger.getLogger(ShowingCodeAndToken.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        document.getClass();
        String code;
        try {
            code = gettingPdfText(document);

            PDPage pagina = document.getPages().get(0);
            int imgToReadLimit = document.isEncrypted() ? 32 : 16;
            GetImageToken printer = new GetImageToken(imgToReadLimit);
            printer.processPage(pagina);
            Map<Integer, ImageIcon> imageic = printer.getImageic();
            String token = "";
            if (document.isEncrypted()) {
                Map<Integer, CodeTokenModel> tokenOrd = new TreeMap<>();
                for (int i : imageic.keySet()) {
                    if (i > imageic.size()) {
                        break;
                    }
                    String ind = crackImage("img" + i + ".jpg");
                    String tok = crackImage("img" + (i + imageic.size()) + ".jpg");
                    tokenOrd.put(Integer.parseInt(ind), new CodeTokenModel(tok, imageic.get(i + imageic.size())));
                }
                List<ImageIcon> tokenOrdImage = new LinkedList<>();

                for (int i : tokenOrd.keySet()) {
                    tokenOrdImage.add(tokenOrd.get(i).getImageToken());
                    token += tokenOrd.get(i).getToken();
                }
                setTextAndImage(code, token, tokenOrdImage);
            } else {
                for (int i : imageic.keySet()) {
                    token += crackImage("img" + i + ".jpg");
                }
                setTextAndImage(code, token, new LinkedList<ImageIcon>(imageic.values()));
            }

            System.out.println(token);

            setVisible(true);
        } catch (IOException ex) {
            Logger.getLogger(ShowingCodeAndToken.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ShowingCodeAndToken.class.getName()).log(Level.SEVERE, null, ex);
        }
//            }
        return true;
    }

    public void run() throws Exception {

        byte[] data = null;
        showing = new ShowingCodeAndToken(gmail);

        data = gmail.getLastToken();
        if (data != null) {
            showing.show(data);
        }
        while (beOn) {
            showing = new ShowingCodeAndToken(gmail);

            data = gmail.getNewTokenPushNotifications();

            if (data != null) {
                showing.show(data);
            } else {
                System.out.println("NO ES TOKEN");
            }
        }

    }

    @Override
    public void dispose() {
        super.dispose(); //To change body of generated methods, choose Tools | Templates.
        System.out.println("...droping...");
        if (gmail != null) {
            gmail.tearDown();
        } else {
            System.out.println("Gmail service NULL");
        }
        beOn = false;
        System.out.println("...droping finished...");
        try {
            this.finalize();

        } catch (Throwable ex) {
            Logger.getLogger(ShowingCodeAndToken.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String... args) {
        try {
            System.out.println("INICIANDOOO...");
            String credentials = ShowingCodeAndToken.class.getResource("/tokenmexitel-1554436818172-185fb0ec2647.json").getPath();
            updateEnv("GOOGLE_APPLICATION_CREDENTIALS", credentials);

            TokenNotifications gmail = null;

            if (args.length != 0) {
                gmail = new TokenNotifications(true);
            } else {
                gmail = new TokenNotifications(false);
            }
            ShowingCodeAndToken showing = new ShowingCodeAndToken(gmail);
            showing.run();
        } catch (Exception ex) {
            Logger.getLogger(ShowingCodeAndToken.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @SuppressWarnings({"unchecked"})
    public static void updateEnv(String name, String val) throws ReflectiveOperationException {
        Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
        Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
        theEnvironmentField.setAccessible(true);
        Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
        env.clear();
        env.put(name, val);
        Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
        theCaseInsensitiveEnvironmentField.setAccessible(true);
        Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
        cienv.clear();
        cienv.put(name, val);

    }

//
//    public void run(String pdf) {
//
//        ShowingCodeAndToken window = new ShowingCodeAndToken();
//
//        try (PDDocument document = PDDocument.load(new File(pdf))) {
//
//            document.getClass();
//            if (!document.isEncrypted()) {
//                String code = window.gettingPdfText(document);
//
//                PDPage pagina = document.getPages().get(0);
//                GetImageToken printer = new GetImageToken();
//                printer.processPage(pagina);
//                ImageIcon imageic = printer.getImageic();
//
//                String token = crackImage("testing.jpg");
//
//                window.setTextAndImage(code, token, imageic);
//                window.setVisible(true);
//            }
//        } catch (Exception ex) {
//            Logger.getLogger(ShowingCodeAndToken.class.getName()).log(Level.SEVERE, null, ex);
//            JOptionPane.showMessageDialog(null, "Error de impresion", "Error", JOptionPane.ERROR_MESSAGE);
//        }
//    }
    public ShowingCodeAndToken(TokenNotifications gmail) throws Exception {
        super();
        this.gmail = gmail;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Online First Person Shooter");
        setAlwaysOnTop(true);
//        setResizable(false);
        setLocation(350, 5);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    }

    public void setTextAndImage(String code, String token, List<ImageIcon> image) throws BadLocationException {

        JPanel codeimages = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel PdfPasswordInfo = new JPanel(new FlowLayout(FlowLayout.LEADING));

        JTextField labelText = new JTextField(code);
        labelText.setEditable(false);
//        labelText.setSelectionStart(7);
        labelText.setFont(new Font(code, 0, 18));
        add(labelText, BorderLayout.EAST);

//        LinkedList<JLabel> labeels = new LinkedList<>();
        for (int i = 0; i < image.size(); i++) {
            JLabel labelImage = new JLabel(image.get(i));
//            labeels.add(labelImage);
//            JScrollPane scrollPane = new JScrollPane(labelImage);
//            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            codeimages.add(labelImage);
        }
        add(codeimages, BorderLayout.CENTER);

        JTextField labelToken = new JTextField(token);
        labelToken.setEditable(false);
        labelToken.setFont(new Font("mine", 0, 30));
        labelToken.selectAll();
        add(labelToken, BorderLayout.WEST);

        JTextField newPassword = new JTextField(10);
        newPassword.setFont(new Font("mine", 0, 20));
        JLabel actualPassword = new JLabel("CONTRASEÑA: " + contraseñaPdf);
        actualPassword.setFont(new Font("mine", 0, 18));
        JButton pdfPassword = new JButton("ACTUALIZAR CONTRASEÑA PDF");
        pdfPassword.setFont(new Font("mine", 0, 18));

        pdfPassword.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                contraseñaPdf = newPassword.getText();
                actualPassword.setText("CONTRASEÑA: " + contraseñaPdf);
            }
        });
        PdfPasswordInfo.add(newPassword);
        PdfPasswordInfo.add(actualPassword);
        PdfPasswordInfo.add(pdfPassword);

        add(PdfPasswordInfo, BorderLayout.AFTER_LAST_LINE);

        pack();

        StringSelection selection = new StringSelection(code.substring(7, code.length()));
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    public String gettingPdfText(PDDocument document) throws IOException {
        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
        stripper.setSortByPosition(true);
        PDFTextStripper tStripper = new PDFTextStripper();
        String pdfFileInText = tStripper.getText(document);
        //System.out.println("Text:" + st);
        // split by whitespace
        String returning = "";
        String lines[] = pdfFileInText.split("\\r?\\n");
        for (String line : lines) {
            if (line.contains("Token:")) {
                returning = line;
            }
        }
        return returning;
    }

    public String crackImage(String f) throws IOException, Exception {

        BasicTesseractExampleTest tst = new BasicTesseractExampleTest();
        return tst.givenTessBaseApi_whenImageOcrd_thenTextDisplayed(f).trim().replaceAll(" ", "");

    }

}
