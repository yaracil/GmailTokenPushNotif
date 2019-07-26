/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package model;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import model.ClientConPer;
import model.ClienteClass;
import model.EmailAccount;
import model.TipoCliente;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Lists;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import model.ClientPanama;
import model.ClientSinPermisoTurismo;

/**
 *
 * @author yoelkys.hernandez-h
 */
public class DataGetter {

    private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens/shhets";

    final NetHttpTransport HTTP_TRANSPORT;
    final String spreadsheetId = "1AJaergqydb8yRag1CaXQ1DxtGv1n72VfoDT5i2XMSUY";

    /**
     * Global instance of the scopes required by this quickstart. If modifying
     * these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    LinkedList<ClienteClass> clientes;
    int limiteClientesXCuenta;

    EmailAccount cuenta;

    public DataGetter() throws GeneralSecurityException, IOException {
        clientes = new LinkedList<>();
        cuenta = null;
        HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        this.limiteClientesXCuenta = 4;
    }

    public final EmailAccount getSystemAccount(String pcId) throws GeneralSecurityException, IOException {

        LinkedList<EmailAccount> cuentas = new LinkedList<>();
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String spreadsheetId = "1AJaergqydb8yRag1CaXQ1DxtGv1n72VfoDT5i2XMSUY";
        final String range = "CUENTAS!A2:D";
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            int pos = 0;
            for (List row : values) {
                if (row.get(0).equals(pcId)) {
                    //PC ASIGNADA   |   CORREO  |   CONTRASEÑA  |   EN USO
                    // EmailAccount(String email, String contrasenna)               
                    EmailAccount acc = new EmailAccount((String) row.get(1), (String) row.get(2), (String) row.get(3), pos);
                    cuentas.add(acc);
                    // Print columns A and E, which correspond to indices 0 and 4.
                    for (Object col : row) {
                        System.out.print(col + ", ");
                    }
                    System.out.println("");
                }
                pos++;
            }
        }
        for (int i = 0; i < cuentas.size(); i++) {
            if (!cuentas.get(i).getEnUso()) {
                this.cuenta = cuentas.get(i);
                setAccountInUseValue(i, "SI");
                return this.cuenta;
            }
        }
        System.out.println("Alerta: todas las cuentas de correo se encuentran en uso!");
        int rand = (int) (Math.random() * cuentas.size());
        System.out.println("Rand = " + rand);
        this.cuenta = cuentas.get(rand);
        System.out.println(cuentas.get(rand).getEmail());
        return this.cuenta;
    }

    public final void setAccountInUseValue(int accountPos, String value) throws GeneralSecurityException, IOException {

        final String range = "CUENTAS!D" + (accountPos + 2);
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        List<List<Object>> values = Arrays.asList(
                Arrays.asList(value
                )
        // Additional rows ...
        );
        ValueRange body = new ValueRange()
                .setValues(values);
        UpdateValuesResponse result
                = service.spreadsheets().values().update(spreadsheetId, range, body)
                        .setValueInputOption("RAW")
                        .execute();
        System.out.printf("%d cells updated.", result.getUpdatedCells());
    }

    public final void setClientActiveAccountsSinPermisoTurismo(int cliente, String newAccount) throws GeneralSecurityException, IOException {

        final String range = "SIN PERMISO INM TURISMO!H" + (cliente + 2);
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        String accounts = clientes.get(cliente).getCuentasUsandoloToString().equals("") ? newAccount : clientes.get(cliente).getCuentasUsandoloToString() + "," + newAccount;
        List<List<Object>> values = Arrays.asList(
                Arrays.asList(accounts
                )
        // Additional rows ...
        );
        ValueRange body = new ValueRange()
                .setValues(values);
        UpdateValuesResponse result
                = service.spreadsheets().values().update(spreadsheetId, range, body)
                        .setValueInputOption("RAW")
                        .execute();
        System.out.printf("%d cells updated.", result.getUpdatedCells());
    }

    public final void setClientePanamaSacado(String pasaporte) throws GeneralSecurityException, IOException, Exception {
        int cliente = getClientPosFromPasaporte(pasaporte);

        final String range = "PANAMA!G" + (cliente + 2);
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
//        String accounts = clientes.get(cliente).getCuentasUsandoloToString().equals("") ? newAccount : clientes.get(cliente).getCuentasUsandoloToString() + "," + newAccount;
        List<List<Object>> values = Arrays.asList(
                Arrays.asList("SACADA"
                )
        // Additional rows ...
        );
        ValueRange body = new ValueRange()
                .setValues(values);
        UpdateValuesResponse result
                = service.spreadsheets().values().update(spreadsheetId, range, body)
                        .setValueInputOption("RAW")
                        .execute();
        System.out.printf("%d cells updated.", result.getUpdatedCells());
    }

    public final void setClientActiveAccountsConPermiso(int cliente, String newAccount) throws GeneralSecurityException, IOException {

        final String range = "CON PERMISO INM!H" + (cliente + 2);
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        String accounts = clientes.get(cliente).getCuentasUsandoloToString().equals("") ? newAccount : clientes.get(cliente).getCuentasUsandoloToString() + "," + newAccount;
        List<List<Object>> values = Arrays.asList(
                Arrays.asList(accounts
                )
        // Additional rows ...
        );
        ValueRange body = new ValueRange()
                .setValues(values);
        UpdateValuesResponse result
                = service.spreadsheets().values().update(spreadsheetId, range, body)
                        .setValueInputOption("RAW")
                        .execute();
        System.out.printf("%d cells updated.", result.getUpdatedCells());
    }

    public int getClientPosFromPasaporte(String pasaporte) throws Exception {

        for (int i = 0; i < clientes.size(); i++) {
            if (clientes.get(i).getPasaporte().equals(pasaporte)) {
                return i;
            }
        }
        throw new Exception("NO SE ENCUENTRA EL PASAPORTE BUSCADO: getClientPosFromPasaporte()");
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = DataGetter.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public final void getClientsSinPermisoTurismo() throws GeneralSecurityException, IOException {

        // Build a new authorized API client service.
        final String range = "SIN PERMISO INM TURISMO!A2:H";
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            for (List row : values) {
                //Prioridad     |   Nombre(s)   |   Apellido 1  |    Apellido 2    |   No. pasaporte   |   Fecha de nacimiento (dd/mm/aaaa)    |   Sexo (H o M)
                //ClientSinPermisoTurismo(String pasaporte, String nombre, String apellido1, String apellido2, TipoCliente tipoCliente, String prioridad, String fecha, String sexo)
                String emailList = (String) row.get(7);
                List<String> accounts;
                if (emailList.contains("@")) {
                    accounts = emailList.contains(",") ? Arrays.asList(emailList.split(",")) : Arrays.asList(emailList);
                } else {
                    accounts = new LinkedList<>();
                }
                ClientSinPermisoTurismo client = new ClientSinPermisoTurismo((String) row.get(4), (String) row.get(1), (String) row.get(2), (String) row.get(3), TipoCliente.SIN_PERMISOINM_TURISMO, (String) row.get(0), (String) row.get(5), (String) row.get(6), accounts);
                clientes.add(client);
                // Print columns A and E, which correspond to indices 0 and 4.
                for (Object col : row) {
                    System.out.print(col + ", ");
                }
                System.out.println("");
            }
        }

    }

    public final void getClientsConPermiso() throws GeneralSecurityException, IOException {

        // Build a new authorized API client service.
        final String range = "CON PERMISO INM!A2:I";
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            for (List row : values) {
                //Prioridad     |   Nombre(s)   |   Apellido 1  |    Apellido 2    |   No. pasaporte   |   Fecha de nacimiento (dd/mm/aaaa)    |   Sexo (H o M)
                //public ClientConPer(String pasaporte, String nombre, String apellido1, String apellido2, String nut, TipoCliente tipoCliente, String prioridad, List<String> cuentasUsandolo) {

                String emailList = (String) row.get(7);
                List<String> accounts;
                if (emailList.contains("@")) {
                    accounts = emailList.contains(",") ? Arrays.asList(emailList.split(",")) : Arrays.asList(emailList);
                } else {
                    accounts = new LinkedList<>();
                }
                ClientConPer client = new ClientConPer((String) row.get(4), (String) row.get(1), (String) row.get(2), (String) row.get(3), (String) row.get(8), TipoCliente.CON_PERMISOINM, (String) row.get(0), accounts);
                clientes.add(client);
                // Print columns A and E, which correspond to indices 0 and 4.
                for (Object col : row) {
                    System.out.print(col + ", ");
                }
                System.out.println("");
            }
        }

    }

    public final void getClientsPanama() throws GeneralSecurityException, IOException {
        // Build a new authorized API client service.
        final String range = "PANAMA!A2:G";
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            for (List row : values) {
                //Nombre	Apellidos	CARNET	 PASAPORTE	FECHA_NACIMIENTO	GENERO	SACADO
                //public ClientConPer(String pasaporte, String nombre, String apellido1, String apellido2, String nut, TipoCliente tipoCliente, String prioridad, List<String> cuentasUsandolo) {

                List<String> accounts = null;
                //ClientPanama(                        String fechaNacimiento, char sexo,      String carnet,      String pasaporte,   String nombre,        String apellidos1,                    String apellidos2,                  TipoCliente tipoCliente, String prioridad, List<String> cuentasUsandolo, String sacado)
                ClientPanama client = new ClientPanama((String) row.get(4), (String) row.get(5), (String) row.get(2), (String) row.get(3), (String) row.get(0), (String) row.get(1), "", TipoCliente.PANAMA, "1", accounts, (String) row.get(6));
                clientes.add(client);
                // Print columns A and E, which correspond to indices 0 and 4.
                for (Object col : row) {
                    System.out.print(col + ", ");
                }
                System.out.println("");
            }
        }

    }

    public ClienteClass requestCliente(String emailAccount, TipoCliente tipoCliente) throws Exception {
        LinkedList<ClienteClass> aux = (LinkedList<ClienteClass>) clientes.clone();
        Collections.sort(aux, new Comparator<ClienteClass>() {
            @Override
            public int compare(ClienteClass t, ClienteClass t1) {
                return new Integer(t.getPrioridad()).compareTo(new Integer(t1.getPrioridad()));
            }
        });
        for (int i = 0; i < aux.size(); i++) {
            ClienteClass out = aux.get(i);
            if (out.getCantCuentasUsandolo() < limiteClientesXCuenta) {
                if (tipoCliente.equals(TipoCliente.SIN_PERMISOINM_TURISMO)) {
                    setClientActiveAccountsSinPermisoTurismo(getClientPosFromPasaporte(out.getPasaporte()), emailAccount);
                } else {
                    setClientActiveAccountsConPermiso(getClientPosFromPasaporte(out.getPasaporte()), emailAccount);

                }
                return out;
            }
        }
        System.out.println("Alerta: los clientes estan sobreusados (muchas cuentas y pocos cientes)!");
        return clientes.get(0);
    }

    public ClientPanama requestRandomClientPanama() throws Exception {
        int tries = 0;
        while (tries < 80) {
            int randomPos = (int) (Math.random() * clientes.size());
            if (!((ClientPanama) clientes.get(randomPos)).getSacado()) {
                System.out.println("Devolviendo clientes de Panama " + randomPos);
                return (ClientPanama) clientes.get(randomPos);
            }
            System.out.println("Saltando cliente ya sacado " + randomPos);
            tries++;
        }
        throw new Exception("YA SE SACARON TODAS LAS CITAS");
    }
}
