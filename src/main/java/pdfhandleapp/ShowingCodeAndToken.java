package pdfhandleapp;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.google.api.services.gmail.Gmail;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
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

    public void show(byte[] pdfData) {

        try (PDDocument document = PDDocument.load(pdfData)) {

            document.getClass();
            if (!document.isEncrypted()) {
                String code = gettingPdfText(document);

                PDPage pagina = document.getPages().get(0);
                GetImageToken printer = new GetImageToken();
                printer.processPage(pagina);
                LinkedList<ImageIcon> imageic = printer.getImageic();

                String token = "";
                for (int i = 2; i <= 16; i += 2) {
                    token += crackImage("img" + i + ".jpg");
                }
                setTextAndImage(code, token, imageic);
                setVisible(true);
            }
        } catch (Exception ex) {
            Logger.getLogger(ShowingCodeAndToken.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Error de impresion", "Error", JOptionPane.ERROR_MESSAGE);
        }
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
            TokenNotifications gmail = new TokenNotifications();

            ShowingCodeAndToken showing = new ShowingCodeAndToken(gmail);
            showing.run();
        } catch (Exception ex) {
            Logger.getLogger(ShowingCodeAndToken.class.getName()).log(Level.SEVERE, null, ex);
        }

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

    public void setTextAndImage(String code, String token, LinkedList<ImageIcon> image) throws BadLocationException {

        JPanel codeimages = new JPanel(new FlowLayout(FlowLayout.LEFT));

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
