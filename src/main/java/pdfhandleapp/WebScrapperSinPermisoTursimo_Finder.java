/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pdfhandleapp;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import model.ClientConPer;
import model.ClientSinPermisoTurismo;
import model.ClienteClass;
import model.DataGetter;
import model.EmailAccount;
import model.TipoCliente;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import pdfhandleapp.BasicTesseractExampleTest;
import pdfhandleapp.GetImageToken;
import pdfhandleapp.ShowingCodeAndToken;

public class WebScrapperSinPermisoTursimo_Finder extends JFrame {

    String mes = "Agosto";
    String idPc = "1";

    static int COUNT_VPN = 0;

    public WebDriver driver;
    WebDriverWait wait;
    JavascriptExecutor jse;
    Actions action;
    ClienteClass cliente;
    EmailAccount cuenta;
    Scanner object;
    DataGetter dataIn;

    static String vpnInfo[][] = {
        {"us-central.windscribe.com", "57s4rafx-f8635ya", "9ezj2pygwx"},
        {"us-east.windscribe.com", "57s4rafx-f8635ya", "9ezj2pygwx"},
        {"us-west.windscribe.com", "57s4rafx-f8635ya", "9ezj2pygwx"},
        {"ca.windscribe.com", "57s4rafx-f8635ya", "9ezj2pygwx"},
        {"ca-west.windscribe.com", "57s4rafx-f8635ya", "9ezj2pygwx"}};

    public WebScrapperSinPermisoTursimo_Finder() throws GeneralSecurityException, IOException, Exception {
        this.setAlwaysOnTop(true);
        driver = new FirefoxDriver();
        wait = new WebDriverWait(driver, 120);
        jse = (JavascriptExecutor) driver;
        action = new Actions(driver);

        object = new Scanner(System.in);

        updateInfo();

    }

    public void updateInfo() throws GeneralSecurityException, IOException, Exception {

        dataIn = new DataGetter();
        dataIn.getClientsSinPermisoTurismo();
        this.cuenta = dataIn.getSystemAccount(idPc);
        this.cliente = dataIn.requestCliente(cuenta.getEmail(), TipoCliente.SIN_PERMISOINM_TURISMO);
    }

    /**
     * Open the test website.
     */
    public void openTestSite() {
        driver.navigate().to("https://mexitel.sre.gob.mx/citas.webportal/pages/public/login/login.jsf");
        WebElement until = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("j_username")));
        until.click();
    }

    public void runFill() throws InterruptedException {
        try {
            paisFill();
            tipoDocumentoFill();
            sinPermisoTurismoFill();
//            legalizacionFill();
            validarCatcha();
            saveScreenshot();
            if (buscarCalendario()) {
                buscarCalendario();
                Thread.sleep(4000);
//                ponerToken();
            }
        } catch (Exception ex) {
            Logger.getLogger(WebScrapperSinPermisoTursimo_Finder.class.getName()).log(Level.SEVERE, null, ex);
            if (JOptionPane.showConfirmDialog(this, "Error en llenado, quiere reintentar desde el inicio?") == 0) {
                limpiarReiniciar();

            }
        }

    }

    public void limpiarReiniciar() throws InterruptedException {
        WebElement limpiar = wait.until(ExpectedConditions.elementToBeClickable(By.id("formRegistroCitaExtranjero:limpiarCita")));
//        action.moveToElement(limpiar).click().build().perform();
        limpiar.click();
        Thread.sleep(3000);
//        driver.switchTo().defaultContent();
        runFill();
    }

    /**
     *
     * Logins into the website, by entering provided username and password
     */
    public void login() {

        WebElement userName_editbox = driver.findElement(By.id("j_username"));
        WebElement password_editbox = driver.findElement(By.id("j_password"));
        WebElement submit_button = driver.findElement(By.xpath("//input[@value='Ingresar']"));

        userName_editbox.sendKeys(cuenta.getEmail());
        password_editbox.sendKeys(cuenta.getContrasenna());
        submit_button.click();
    }

    public void conPermisoFill() throws InterruptedException {
        //TIPO DE DOCUMENTO TRAMITE   
        WebElement tramite = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("formRegistroCitaExtranjero:selectTramite_input")));
        action.moveToElement(tramite);
        tramite.sendKeys("CON");
//        tramite.sendKeys(Keys.ENTER);
        Thread.sleep(3000);

        //TIPO DE DOCUMENTO NUT
        wait.until(ExpectedConditions.presenceOfElementLocated((By.id("formRegistroCitaExtranjero:noPasapNUT"))));
        WebElement nut = driver.findElement((By.id("formRegistroCitaExtranjero:noPasapNUT")));
        action.moveToElement(nut);
        nut.sendKeys(((ClientConPer) cliente).getNut());
        nut.sendKeys(Keys.ENTER);
        Thread.sleep(3000);

        //TIPO DE DOCUMENTO pasaporte
        wait.until(ExpectedConditions.elementToBeClickable(By.id("formRegistroCitaExtranjero:noPasapAnt")));
        WebElement pasaporte = driver.findElement((By.id("formRegistroCitaExtranjero:noPasapAnt")));
        action.moveToElement(pasaporte);
        pasaporte.sendKeys(cliente.getPasaporte());
        pasaporte.sendKeys(Keys.TAB);
        Thread.sleep(3000);

        //VALIDAR NUT
//        WebElement tabla = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("formRegistroCitaExtranjero:correo")));
//        action.moveToElement(tabla);
//        action.moveToElement(tabla).click().build().perform();
////        tabla.click();
//        Thread.sleep(3000);
        //
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("formRegistroCitaExtranjero:btnValidarNUT")));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("formRegistroCitaExtranjero:btnValidarNUT")));
        WebElement button_validar = driver.findElement(By.id("formRegistroCitaExtranjero:btnValidarNUT"));
//        action.moveToElement(button_validar);
        button_validar.click();
//        action.moveToElement(button_validar).click().build().perform();
        Thread.sleep(3000);
        button_validar.click();
    }

    public void sinPermisoTurismoFill() throws InterruptedException {
        //TIPO DE DOCUMENTO TRAMITE 
        WebElement tramite = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("formRegistroCitaExtranjero:selectTramite_input")));
        action.moveToElement(tramite);
        tramite.sendKeys("SIN");
//        tramite.sendKeys(Keys.ENTER);
        Thread.sleep(3000);

        //selectTipoTramite        
        WebElement tipoTram = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("formRegistroCitaExtranjero:selectTipoTramite_input")));
//        action.moveToElement(tipoTram);
        tipoTram.sendKeys("TURISMO");
//        tramite.sendKeys(Keys.ENTER);
        Thread.sleep(3000);

        //TIPO DE DOCUMENTO pasaporte
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("formRegistroCitaExtranjero:selectTipoTramiteDetalle_label"), "TURISMO Y TRANSITO"));
        WebElement pasaporte = wait.until(ExpectedConditions.elementToBeClickable(By.id("formRegistroCitaExtranjero:noPasapAnt")));
//         = driver.findElement((By.id("formRegistroCitaExtranjero:noPasapAnt")));
        action.moveToElement(pasaporte);
        pasaporte.sendKeys(cliente.getPasaporte());
//        pasaporte.sendKeys(Keys.TAB);
        Thread.sleep(3000);

//PAIS PASAPORTE
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("formRegistroCitaExtranjero:selectPaisPasaporte_input")));
        WebElement paisPasap = wait.until(ExpectedConditions.elementToBeClickable(By.id("formRegistroCitaExtranjero:selectPaisPasaporte")));
//        action.moveToElement(paisPasap).click().build().perform();
        paisPasap.click();
        Thread.sleep(3000);
//        element.click();

        WebElement element2 = wait.until(ExpectedConditions.elementToBeClickable(By.id("formRegistroCitaExtranjero:selectPaisPasaporte_filter")));
        element2.sendKeys("CUBA");
        element2.sendKeys(Keys.BACK_SPACE);
        element2.sendKeys(Keys.ENTER);
        Thread.sleep(3000);

        //NAME
        WebElement nombre = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("formRegistroCitaExtranjero:nombre")));
//        action.moveToElement(nombre).click().build().perform();        
        nombre.sendKeys(cliente.getNombre());
        nombre.sendKeys(Keys.TAB);
        Thread.sleep(3000);

        //formRegistroCitaExtranjero:ApellidoPat
        WebElement apellidoPat = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("formRegistroCitaExtranjero:Apellidos")));
        apellidoPat.sendKeys(cliente.getApellidos1() + " " + cliente.getApellidos2());
        apellidoPat.sendKeys(Keys.TAB);
        Thread.sleep(3000);

        //formRegistroCitaExtranjero:selectNacionalidad_label
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("formRegistroCitaExtranjero:selectNacionalidad_label")));
        WebElement nacionalidad = wait.until(ExpectedConditions.elementToBeClickable(By.id("formRegistroCitaExtranjero:selectNacionalidad_label")));
//        action.moveToElement(paisPasap).click().build().perform();
        nacionalidad.click();
        Thread.sleep(3000);
//        element.click();

        WebElement nacionalidad1 = wait.until(ExpectedConditions.elementToBeClickable(By.id("formRegistroCitaExtranjero:selectNacionalidad_filter")));
        nacionalidad1.sendKeys("CUBA");
        nacionalidad1.sendKeys(Keys.BACK_SPACE);
        nacionalidad1.sendKeys(Keys.ENTER);
        Thread.sleep(3000);

        //formRegistroCitaExtranjero:fechaNacimiento_input        
        WebElement fecha = wait.until(ExpectedConditions.elementToBeClickable(By.id("formRegistroCitaExtranjero:fechaNacimiento_input")));
        fecha.sendKeys(((ClientSinPermisoTurismo) cliente).getFechaNacimiento());
        Thread.sleep(3000);

        //formRegistroCitaExtranjero:selectPaisNacimiento_label
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("formRegistroCitaExtranjero:selectPaisNacimiento_label")));
        WebElement paisNacimiento = wait.until(ExpectedConditions.elementToBeClickable(By.id("formRegistroCitaExtranjero:selectPaisNacimiento_label")));
        paisNacimiento.click();
        Thread.sleep(3000);

        WebElement paisNacimiento1 = wait.until(ExpectedConditions.elementToBeClickable(By.id("formRegistroCitaExtranjero:selectPaisNacimiento_filter")));
        paisNacimiento1.sendKeys("CUBA");
        paisNacimiento1.sendKeys(Keys.BACK_SPACE);
        paisNacimiento1.sendKeys(Keys.ENTER);
        Thread.sleep(3000);

        //sexo
        WebElement sexo = wait.until(ExpectedConditions.elementToBeClickable(By.id("formRegistroCitaExtranjero:sexo_input")));
        sexo.sendKeys("M");
        Thread.sleep(3000);
    }

    public void paisFill() throws InterruptedException {
        //presence in DOM
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("formRegistroCitaExtranjero:selectPais_input")));
        wait.until(ExpectedConditions.elementToBeClickable(By.id("formRegistroCitaExtranjero:selectPais_input")));
        WebElement element = driver.findElement(By.id("formRegistroCitaExtranjero:selectPais_input"));
        action.moveToElement(element);
        action.moveToElement(element).click().build().perform();
//        element.click();

        WebElement element2 = wait.until(ExpectedConditions.elementToBeClickable(By.id("formRegistroCitaExtranjero:selectPais_filter")));
        element2.sendKeys("CUBA");
        element2.sendKeys(Keys.BACK_SPACE);
        element2.sendKeys(Keys.ENTER);
        Thread.sleep(3000);

        //TIPO DE DOCUMENTO VISA
        // WebElement sedeUbiHija_label = wait.until(ExpectedConditions.elementToBeClickable(By.id("formRegistroCitaExtranjero:sedeUbiHija_label")));
        try {
            wait.until(ExpectedConditions.textToBe(By.id("formRegistroCitaExtranjero:sedeUbiHija_label"), "LA HABANA"));
        } catch (Exception ex) {
            Logger.getLogger(WebScrapperSinPermisoTursimo_Finder.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("No se encontro oficina de La Habana. Repitiendo...");
            paisFill();
        }
    }

    public void tipoDocumentoFill() throws InterruptedException {
        WebElement documento = wait.until(ExpectedConditions.elementToBeClickable(By.id("formRegistroCitaExtranjero:selectTipoDocumento_input")));
        action.moveToElement(documento).click().build().perform();
        documento.sendKeys("VISA");
        documento.sendKeys(Keys.ENTER);
        Thread.sleep(3000);
    }
    //formRegistroCitaExtranjero:selectNoLegalizados_input

    public void legalizacionFill() throws InterruptedException {
//        wait.until(ExpectedConditions.textToBe(By.id("formRegistroCitaExtranjero:selectTipoDocumento_inputl"), "VISAS"));
        WebElement documento = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("formRegistroCitaExtranjero:selectNoLegalizados_input")));
//        action.moveToElement(documento).click().build().perform();
//        documento.click();
        documento.sendKeys("1");
        documento.sendKeys(Keys.TAB);
        Thread.sleep(3000);

//        WebElement pasaporte = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("formRegistroCitaExtranjero:doc:0:noMinrex")));
////        action.moveToElement(pasaporte).click().build().perform();
//        pasaporte.sendKeys("K140028234");
//        pasaporte.sendKeys(Keys.ENTER);
//        Thread.sleep(3000);
////        pasaporte.sendKeys(Keys.ENTER);
////formRegistroCitaExtranjero:nombre
//        WebElement nombre = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("formRegistroCitaExtranjero:nombre")));
////        action.moveToElement(nombre).click().build().perform();
//        Thread.sleep(3000);
//        nombre.sendKeys(cliente.getNombre());
//        System.out.println(cliente.getNombre());
//        Thread.sleep(3000);
//
//        //formRegistroCitaExtranjero:ApellidoPat
//        wait.until(ExpectedConditions.textToBePresentInElementValue(By.id("formRegistroCitaExtranjero:nombre"), cliente.getNombre()));
//        WebElement apellidoPat = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("formRegistroCitaExtranjero:ApellidoPat")));
////        action.moveToElement(apellidoPat).click().build().perform();
//        Thread.sleep(3000);
//        apellidoPat.sendKeys(cliente.getApellidos1());
//        Thread.sleep(3000);
//
//        //formRegistroCitaExtranjero:ApellidoMat
//        WebElement apellidoMat = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("formRegistroCitaExtranjero:ApellidoMat")));
////        action.moveToElement(apellidoMat).click().build().perform();
//        Thread.sleep(3000);
//        apellidoMat.sendKeys(cliente.getApellidos2());
//        Thread.sleep(3000);
//
//        //formRegistroCitaExtranjero:fechaNacimiento_input
//        WebElement fecha = wait.until(ExpectedConditions.elementToBeClickable(By.id("formRegistroCitaExtranjero:fechaNacimiento_input")));
//        fecha.sendKeys("008");
//        fecha.sendKeys("12");
//        fecha.sendKeys("1990");
//        Thread.sleep(3000);
//
//        //sexo
//        WebElement sexo = wait.until(ExpectedConditions.elementToBeClickable(By.id("formRegistroCitaExtranjero:sexo_input")));
////        action.moveToElement(apellidoMat).click().build().perform();
//        Thread.sleep(3000);
//        sexo.sendKeys("M");
//        Thread.sleep(3000);
    }

    public void validarCatcha() throws InterruptedException {
        //formRegistroCitaExtranjero:nombre
//        WebElement nombre = driver.findElement(By.id("formRegistroCitaExtranjero:nombre"));
//        wait.until(ExpectedConditions.attributeToBeNotEmpty(nombre, "value"));
//        WebElement catcha = wait.until(ExpectedConditions.elementToBeClickable(By.id("recaptcha-checkbox-checkmark")));
//        action.moveToElement(catcha).click().build().perform();

        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("formRegistroCitaExtranjero:schedule_container")));
            wait.until(ExpectedConditions.textToBePresentInElementLocated(By.className("fc-center"), mes));
            Thread.sleep(3000);

        } catch (Exception ex) {
            Logger.getLogger(WebScrapperSinPermisoTursimo_Finder.class.getName()).log(Level.SEVERE, null, ex);
            if (JOptionPane.showConfirmDialog(this, "No se detecta calendario valido, seleccione SI para reitentar y NO para reiniciar llenado") == 0) {
                validarCatcha();

            } else {
//                limpiarReiniciar();
                runFill();
            }
        }
    }

    public boolean buscarCalendario() throws InterruptedException {

        boolean diaSelected = false;
        //rangoAltaDisponibilidad        
        WebElement calendario = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("formRegistroCitaExtranjero:schedule_container")));
        List<WebElement> diasTotD = calendario.findElements(By.className("rangoTotalDisponibilidad"));
        if (diasTotD.size() > 0) {
            int rand = (int) (diasTotD.size() * Math.random());
            action.moveToElement(diasTotD.get(rand)).click().build().perform();
            diaSelected = true;
        } else {
            List<WebElement> diasAltDis = calendario.findElements(By.className("rangoAltaDisponibilidad"));
            if (diasAltDis.size() > 0) {
                int rand = (int) (diasAltDis.size() * Math.random());
                action.moveToElement(diasAltDis.get(rand)).click().build().perform();
                diaSelected = true;
            } else {
                List<WebElement> diasRangMod = calendario.findElements(By.className("rangoModerado"));
                if (diasRangMod.size() > 0) {
                    int rand = (int) (diasRangMod.size() * Math.random());
                    action.moveToElement(diasRangMod.get(rand)).click().build().perform();
                    diaSelected = true;
                }
            }
        }
        if (diaSelected) {
            Thread.sleep(3000);
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "SE ACABARON LAS CITAS!!");
            return false;
        }
    }

//    public void ponerToken() throws IOException, GeneralSecurityException {
//        //confirmDialog
//
//        try {
//            WebElement dialogConf = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("confirmDialog")));
//
//            WebElement codigo = dialogConf.findElement(By.id("reviewForm:confirmCodigoSeguridad"));
//            WebElement token = dialogConf.findElement(By.id("reviewForm:confirmToken"));
//
//            Thread.sleep(3000);
//            String[] tokenAndCode = gettingCodeAndText();
////        String codigo = tokenAndCode[1];
////        String token = tokenAndCode[0].substring(7);
//
//            if (tokenAndCode != null) {
//                codigo.sendKeys(tokenAndCode[1] + "nm");
//                token.sendKeys(tokenAndCode[0].substring(7));
//
//                WebElement submit_button = dialogConf.findElement(By.id("reviewForm:confirmarCita"));
////                submit_button.click();
//                //MOSTRAR VENTANA
//                ShowingCodeAndToken shw = new ShowingCodeAndToken();
//                shw.gettingEmail();
//            }
//        } catch (Exception ex) {
//            Logger.getLogger(WebScrapperSinPermisoTursimo_Finder.class.getName()).log(Level.SEVERE, null, ex);
//            if (JOptionPane.showConfirmDialog(this, "No se detecta dialogo confirmacion. reintentar?") == 0) {
//                ponerToken();
//
//            }
//        }
//    }
    /**
     * Saves the screenshot
     *
     * @throws IOException
     */
    public void saveScreenshot() throws IOException {
        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(scrFile, new File("screenshot.png"));
    }

    public void closeBrowser() throws GeneralSecurityException, IOException {
        driver.close();
        dataIn.setAccountInUseValue(cuenta.getPosicionEnDatos(), "NO");
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException {

        System.setProperty("webdriver.gecko.driver", "webdriver.gecko\\geckodriver.exe");
        while (true) {
            try {
                switchVPN();
            } catch (InterruptedException ex) {
                Logger.getLogger(WebScrapperSinPermisoTursimo_Finder.class.getName()).log(Level.SEVERE, null, ex);
            }
            WebScrapperSinPermisoTursimo_Finder webSrcapper = null;
            try {
                webSrcapper = new WebScrapperSinPermisoTursimo_Finder();
                System.out.println("Iniciando...");
                System.out.println("Llenando formulario usando la cuenta: " + webSrcapper.cuenta.getEmail() + "...");
            } catch (IOException ex) {
                Logger.getLogger(WebScrapperSinPermisoTursimo_Finder.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {
                Logger.getLogger(WebScrapperSinPermisoTursimo_Finder.class.getName()).log(Level.SEVERE, null, ex);
            }

//            System.out.println("Los datos del cliente son: \n Nombre: " + webSrcapper.cliente.getNombre() + " " + webSrcapper.cliente.getApellidos() + "\n Pasaporte: " + webSrcapper.cliente.getPasaporte());
            try {
                webSrcapper.openTestSite();
            } catch (Exception ex) {
                Logger.getLogger(WebScrapperSinPermisoTursimo_Finder.class.getName()).log(Level.SEVERE, null, ex);
                webSrcapper.closeBrowser();
                continue;
            }
            webSrcapper.login();
            try {
                webSrcapper.runFill();
            } catch (InterruptedException ex) {
                Logger.getLogger(WebScrapperSinPermisoTursimo_Finder.class.getName()).log(Level.SEVERE, null, ex);
                Scanner obj = new Scanner(System.in);
                System.out.println("Desea cerrar y reintentar??");
                if (obj.nextLine().equals("si")) {
                    webSrcapper.closeBrowser();
                    continue;
                }
            }

            try {
                webSrcapper.closeBrowser();
            } catch (Exception ex) {
                Logger.getLogger(WebScrapperSinPermisoTursimo_Finder.class.getName()).log(Level.SEVERE, null, ex);
                Scanner obj = new Scanner(System.in);
                System.out.println("Desea cerrar y reintentar??");
                if (obj.nextLine().equals("si")) {
                    webSrcapper.closeBrowser();
                    continue;
                }
            }
        }

    }

    static public void switchVPN() throws IOException, InterruptedException {
        //String command = "powershell.exe  your command";
        //Getting the version
        String newIP = null;
        String oldIP = gettingPublicIP();
        System.out.println("IP ACTUAL:" + oldIP);
        do {
            disconnectVpn();
            if (COUNT_VPN == vpnInfo.length) {
                COUNT_VPN = 0;
            }
            String command = "rasdial " + vpnInfo[COUNT_VPN][0];
            // Executing the command
            executingCommands(command);
            COUNT_VPN++;
            Thread.sleep(3000);

            newIP = gettingPublicIP();

        } while (oldIP.equals(newIP));

        System.out.println("IP NUEVO:" + newIP);

    }

    static public void disconnectVpn() throws IOException {
        for (int i = 0; i < vpnInfo.length; i++) {
            String command = "rasdial " + vpnInfo[i][0] + " /disconnect";
            executingCommands(command);
        }
    }

    static public String gettingPublicIP() throws MalformedURLException, IOException {
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                whatismyip.openStream()));

        return in.readLine(); //you get the IP as a String

    }

    static public void executingCommands(String command) throws IOException {
        System.out.println("Executing the command " + command);
        Process powerShellProcess = Runtime.getRuntime().exec(command);
        // Getting the results
        powerShellProcess.getOutputStream().close();
        String line;
        System.out.println("Standard Output:");
        BufferedReader stdout = new BufferedReader(new InputStreamReader(
                powerShellProcess.getInputStream()));
        while ((line = stdout.readLine()) != null) {
            System.out.println(line);
        }
        stdout.close();
//        System.out.println("Standard Error:");
        BufferedReader stderr = new BufferedReader(new InputStreamReader(
                powerShellProcess.getErrorStream()));
        while ((line = stderr.readLine()) != null) {
            System.out.println(line);
        }
        stderr.close();
        System.out.println("Done");
    }
}
//
//    public String[] gettingCodeAndText() throws IOException, GeneralSecurityException {
//        GmailQuickstart gmail = new GmailQuickstart();
//        byte[] pdfData = gmail.run();
//
//        try (PDDocument document = PDDocument.load(pdfData)) {
//
//            document.getClass();
//            if (!document.isEncrypted()) {
//                String code = gettingPdfText(document);
//
//                PDPage pagina = document.getPages().get(0);
//                GetImageToken printer = new GetImageToken();
//                printer.processPage(pagina);
//
//                String token = crackImage("testing.jpg");
//
//                return new String[]{code, token};
//            }
//        } catch (Exception ex) {
//            Logger.getLogger(ShowingCodeAndToken.class.getName()).log(Level.SEVERE, null, ex);
//            System.out.println("Error de impresion");
//        }
//        return null;
//    }
//
//    public String gettingPdfText(PDDocument document) throws IOException {
//        PDFTextStripperByArea stripper = new PDFTextStripperByArea();
//        stripper.setSortByPosition(true);
//        PDFTextStripper tStripper = new PDFTextStripper();
//        String pdfFileInText = tStripper.getText(document);
//        //System.out.println("Text:" + st);
//        // split by whitespace
//        String returning = "";
//        String lines[] = pdfFileInText.split("\\r?\\n");
//        for (String line : lines) {
//            if (line.contains("Token:")) {
//                returning = line;
//            }
//        }
//        return returning;
//    }
//
//    public String crackImage(String f) throws IOException, Exception {
//
//        BasicTesseractExampleTest tst = new BasicTesseractExampleTest();
//        return tst.givenTessBaseApi_whenImageOcrd_thenTextDisplayed(f).trim().replaceAll(" ", "");
//
//    }
//}

//         element2.submit();
//        
//        WebElement pais2 = element.findElement(By.xpath("//li[@class='ui-selectonemenu-item' @data-label='CUBA']"));
//        pais2.click();
//        element.sendKeys(Keys.PAGE_DOWN);
//        Thread.sleep(3000);
//        
//        element.sendKeys(Keys.PAGE_DOWN);
//        Thread.sleep(3000);
//        element.sendKeys(Keys.PAGE_DOWN);
//        Thread.sleep(3000);
//        element.sendKeys(Keys.PAGE_DOWN);
// Scroll the browser to the element's Y position
//        ((JavascriptExecutor) driver).executeScript("document.getElementById('formRegistroCitaExtranjero:selectPais_input').getElementsByTagName('option')[10].selected");
// Click the element
//        pais2.click();
//clickable
//         
//        jse.executeScript("arguments[0].scrollIntoView(true);", pais2);
//        wait.until(ExpectedConditions.elementToBeClickable(pais2));
//        action.contextClick(pais2).click().build().perform();
//      
//        wait.until(ExpectedConditions)
//        element.sendKeys("CUBA");
//        System.out.println("Estooo" + element.getText());
//        System.out.println(pais2.getText());
//        element.click();
//        element.sendKeys("CUBA");
//        System.out.println("doss"+ pais2.getText());
//
//        action.moveToElement(pais2).click().perform();
//        pais2.click();
//        Select select = new Select(driver.findElement(By.id("formRegistroCitaExtranjero:selectPais_input")));
//        
//        select.selectByValue("17");
//         Thread.sleep(3000);
//        select.selectByIndex(5);
//         Thread.sleep(3000);
//         System.out.println("This "+select.getFirstSelectedOption().getText());
//        WebElement element = driver.findElement(select); // located when button visible
//        Actions actions = new Actions(driver);
//        actions.moveToElement((WebElement) select);
//        actions.perform();
//        jse.executeScript("arguments[0].click();", element);
//        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("formRegistroCitaExtranjero:selectPais_input")));
////        jse.executeScript("document.getElementById('formRegistroCitaExtranjero:selectPais_input').option[6]='selected';");
//        WebElement pais = driver.findElement(By.name("formRegistroCitaExtranjero:selectPais_input"));
////        WebElement submit_button = driver.findElement(By.xpath("//select[@value='17']"));
////        pais.click();
//        
//        jse.executeScript("arguments[0].click();", pais2);
//        System.out.println(pais.);
//        pais.sendKeys("CUBA");
//        By by = By.id("formRegistroCitaExtranjero:selectPais_input");
//        WebElement = waitForPresenceOfEleme(by);
//        Select gender = new Select(driver.findElement(By.name("formRegistroCitaExtranjero:selectPais_input")));
//        gender.selectByValue("17");
//        for (int j = 1; j < 3; j++) {
//            System.out.println(gender.getOptions().get(j).getText());
//        }
//
//        Select dropdown = new Select(driver.findElement(By.name("selectPais_input")));
//        dropdown.selectByIndex(7);
//        WebElement select = driver.findElement(By.id("formRegistroCitaExtranjero:selectPais_input"));
//        List<WebElement> options = select.findElements(By.tagName("option"));
//        for (WebElement option : options) {
//            if ("17".equals(option.getText())) {
//                option.click();
//            }
//        }
//   Thread.sleep(30000);
// Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("status.txt"), "utf-8"));
//  writer.write(text);
//  writer.close();
