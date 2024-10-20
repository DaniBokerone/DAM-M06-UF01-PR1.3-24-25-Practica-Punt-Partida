package com.project.pr13;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.project.pr13.format.AsciiTablePrinter;

/**
 * Classe principal que permet gestionar un fitxer XML de cursos amb opcions per llistar, afegir i eliminar alumnes, 
 * així com mostrar informació dels cursos i mòduls.
 * 
 * Aquesta classe inclou funcionalitats per interactuar amb un fitxer XML, executar operacions de consulta,
 * i realitzar modificacions en el contingut del fitxer.
 */
public class PR132Main {

    private final Path xmlFilePath;
    private static final Scanner scanner = new Scanner(System.in);

    /**
     * Constructor de la classe PR132Main.
     * 
     * @param xmlFilePath Ruta al fitxer XML que conté la informació dels cursos.
     */
    public PR132Main(Path xmlFilePath) {
        this.xmlFilePath = xmlFilePath;
    }

    /**
     * Mètode principal que inicia l'execució del programa.
     * 
     * @param args Arguments passats a la línia de comandament (no s'utilitzen en aquest programa).
     */
    public static void main(String[] args) {
        String userDir = System.getProperty("user.dir");
        Path xmlFilePath = Paths.get(userDir, "data", "pr13", "cursos.xml");

        PR132Main app = new PR132Main(xmlFilePath);
        app.executar();
    }

    /**
     * Executa el menú principal del programa fins que l'usuari decideixi sortir.
     */
    public void executar() {
        boolean exit = false;
        while (!exit) {
            mostrarMenu();
            System.out.print("Escull una opció: ");
            int opcio = scanner.nextInt();
            scanner.nextLine(); // Netegem el buffer del scanner
            exit = processarOpcio(opcio);
        }
    }

    /**
     * Processa l'opció seleccionada per l'usuari.
     * 
     * @param opcio Opció seleccionada al menú.
     * @return True si l'usuari decideix sortir del programa, false en cas contrari.
     */
    public boolean processarOpcio(int opcio) {
        String cursId;
        String nomAlumne;
        switch (opcio) {
            case 1:
                List<List<String>> cursos = llistarCursos();
                imprimirTaulaCursos(cursos);
                return false;
            case 2:
                System.out.print("Introdueix l'ID del curs per veure els seus mòduls: ");
                cursId = scanner.nextLine();
                List<List<String>> moduls = mostrarModuls(cursId);
                imprimirTaulaModuls(moduls);
                return false;
            case 3:
                System.out.print("Introdueix l'ID del curs per veure la llista d'alumnes: ");
                cursId = scanner.nextLine();
                List<String> alumnes = llistarAlumnes(cursId);
                imprimirLlistaAlumnes(alumnes);
                return false;
            case 4:
                System.out.print("Introdueix l'ID del curs on vols afegir l'alumne: ");
                cursId = scanner.nextLine();
                System.out.print("Introdueix el nom complet de l'alumne a afegir: ");
                nomAlumne = scanner.nextLine();
                afegirAlumne(cursId, nomAlumne);
                return false;
            case 5:
                System.out.print("Introdueix l'ID del curs on vols eliminar l'alumne: ");
                cursId = scanner.nextLine();
                System.out.print("Introdueix el nom complet de l'alumne a eliminar: ");
                nomAlumne = scanner.nextLine();
                eliminarAlumne(cursId, nomAlumne);
                return false;
            case 6:
                System.out.println("Sortint del programa...");
                return true;
            default:
                System.out.println("Opció no reconeguda. Si us plau, prova de nou.");
                return false;
        }
    }

    /**
     * Mostra el menú principal amb les opcions disponibles.
     */
    private void mostrarMenu() {
        System.out.println("\nMENÚ PRINCIPAL");
        System.out.println("1. Llistar IDs de cursos i tutors");
        System.out.println("2. Mostrar IDs i títols dels mòduls d'un curs");
        System.out.println("3. Llistar alumnes d’un curs");
        System.out.println("4. Afegir un alumne a un curs");
        System.out.println("5. Eliminar un alumne d'un curs");
        System.out.println("6. Sortir");
    }

    /**
     * Llegeix el fitxer XML i llista tots els cursos amb el seu tutor i nombre d'alumnes.
     * 
     * @return Llista amb la informació dels cursos (ID, tutor, nombre d'alumnes).
     */
    public List<List<String>> llistarCursos() {
        // *************** CODI PRÀCTICA **********************/
        List<List<String>> cursosInfo = new ArrayList<>();

        try {
            Document document = carregarDocumentXML(xmlFilePath);
            XPath xpath = XPathFactory.newInstance().newXPath();
            
            NodeList cursos = (NodeList) xpath.evaluate("/cursos/curs", document, XPathConstants.NODESET);
            
            for (int i = 0; i < cursos.getLength(); i++) {
                Node curs = cursos.item(i);
                
                // CURS
                String id = xpath.evaluate("@id", curs);
                
                // Tutor
                String tutor = xpath.evaluate("tutor", curs);
                
                // Alumnes
                NodeList alumnes = (NodeList) xpath.evaluate("alumnes/alumne", curs, XPathConstants.NODESET);
                int totalAlumnes = alumnes.getLength();

                //Info curs
                List<String> cursInfo = new ArrayList<>();
                cursInfo.add(id);
                cursInfo.add(tutor);
                cursInfo.add(String.valueOf(totalAlumnes));
                cursosInfo.add(cursInfo);
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return cursosInfo;
    }

    /**
     * Imprimeix per consola una taula amb la informació dels cursos.
     * 
     * @param cursos Llista amb la informació dels cursos.
     */
    public void imprimirTaulaCursos(List<List<String>> cursos) {
        List<String> capçaleres = List.of("ID", "Tutor", "Total Alumnes");
        AsciiTablePrinter.imprimirTaula(capçaleres, cursos);
    }

    /**
     * Mostra els mòduls d'un curs especificat pel seu ID.
     * 
     * @param idCurs ID del curs del qual es volen veure els mòduls.
     * @return Llista amb la informació dels mòduls (ID, títol).
     */
    public List<List<String>> mostrarModuls(String idCurs) {
        // *************** CODI PRÀCTICA **********************/
        List<List<String>> modulsInfo = new ArrayList<>();

        try {
            Document document = carregarDocumentXML(xmlFilePath);
            XPath xpath = XPathFactory.newInstance().newXPath();
            
            // Sustituim %s amb l'id del curs
            String expression = String.format("/cursos/curs[@id='%s']/moduls/modul", idCurs);
            NodeList moduls = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
            
            for (int i = 0; i < moduls.getLength(); i++) {
                Node modul = moduls.item(i);
                
                //Modul
                String idModul = xpath.evaluate("@id", modul);
                String titol = xpath.evaluate("titol", modul);
                
                // Info modul
                List<String> modulInfo = new ArrayList<>();
                modulInfo.add(idModul);
                modulInfo.add(titol);
                modulsInfo.add(modulInfo);
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return modulsInfo;
    }

    /**
     * Imprimeix per consola una taula amb la informació dels mòduls.
     * 
     * @param moduls Llista amb la informació dels mòduls.
     */
    public void imprimirTaulaModuls(List<List<String>> moduls) {
        List<String> capçaleres = List.of("ID Mòdul", "Títol");
        AsciiTablePrinter.imprimirTaula(capçaleres, moduls);
    }

    /**
     * Llista els alumnes inscrits en un curs especificat pel seu ID.
     * 
     * @param idCurs ID del curs del qual es volen veure els alumnes.
     * @return Llista amb els noms dels alumnes.
     */
    public List<String> llistarAlumnes(String idCurs) {
        // *************** CODI PRÀCTICA **********************/
        List<String> alumnesInfo = new ArrayList<>();

        try {
            Document document = carregarDocumentXML(xmlFilePath);
            XPath xpath = XPathFactory.newInstance().newXPath();
            
            // Sustituim %s amb l'id del curs
            String expression = String.format("/cursos/curs[@id='%s']/alumnes/alumne", idCurs);
            NodeList alumnes = (NodeList) xpath.evaluate(expression, document, XPathConstants.NODESET);
            
            for (int i = 0; i < alumnes.getLength(); i++) {
                Node alumne = alumnes.item(i);
                alumnesInfo.add(alumne.getTextContent());
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
        return alumnesInfo;
    }

    /**
     * Imprimeix per consola la llista d'alumnes d'un curs.
     * 
     * @param alumnes Llista d'alumnes a imprimir.
     */
    public void imprimirLlistaAlumnes(List<String> alumnes) {
        System.out.println("Alumnes:");
        alumnes.forEach(alumne -> System.out.println("- " + alumne));
    }

    /**
     * Afegeix un alumne a un curs especificat pel seu ID.
     * 
     * @param idCurs ID del curs on es vol afegir l'alumne.
     * @param nomAlumne Nom de l'alumne a afegir.
     */
    public void afegirAlumne(String idCurs, String nomAlumne) {
        // *************** CODI PRÀCTICA **********************/
        try {
            Document document = carregarDocumentXML(xmlFilePath);
            XPath xpath = XPathFactory.newInstance().newXPath();
            
            // Sustituim %s amb l'id del curs
            String expression = String.format("/cursos/curs[@id='%s']/alumnes", idCurs);
            Node alumnesNode = (Node) xpath.evaluate(expression, document, XPathConstants.NODE);
            
            if (alumnesNode != null) {
                // Element alumne
                Element nouAlumne = document.createElement("alumne");
                nouAlumne.setTextContent(nomAlumne);
                
                // Afegir als demes alumnes
                alumnesNode.appendChild(nouAlumne);
                
                guardarDocumentXML(document);
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Elimina un alumne d'un curs especificat pel seu ID.
     * 
     * @param idCurs ID del curs d'on es vol eliminar l'alumne.
     * @param nomAlumne Nom de l'alumne a eliminar.
     */
    public void eliminarAlumne(String idCurs, String nomAlumne) {
        // *************** CODI PRÀCTICA **********************/
        try {
            Document document = carregarDocumentXML(xmlFilePath);
            XPath xpath = XPathFactory.newInstance().newXPath();
            
            // Sustituim %s amb l'id del curs / nom alumne
            String expression = String.format("/cursos/curs[@id='%s']/alumnes/alumne[text()='%s']", idCurs, nomAlumne);
            Node alumneNode = (Node) xpath.evaluate(expression, document, XPathConstants.NODE);
            
            if (alumneNode != null) {
                alumneNode.getParentNode().removeChild(alumneNode);
                
                guardarDocumentXML(document);
            }
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Carrega el document XML des de la ruta especificada.
     * 
     * @param pathToXml Ruta del fitxer XML a carregar.
     * @return Document XML carregat.
     */
    private Document carregarDocumentXML(Path pathToXml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(pathToXml.toFile());
        } catch (Exception e) {
            throw new RuntimeException("Error en carregar el document XML.", e);
        }
    }

    /**
     * Guarda el document XML proporcionat en la ruta del fitxer original.
     * 
     * @param document Document XML a guardar.
     */
    private void guardarDocumentXML(Document document) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(xmlFilePath.toFile());
            transformer.transform(source, result);
            System.out.println("El fitxer XML ha estat guardat amb èxit.");
        } catch (TransformerException e) {
            System.out.println("Error en guardar el fitxer XML.");
            e.printStackTrace();
        }
    }
}
