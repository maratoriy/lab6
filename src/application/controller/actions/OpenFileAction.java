package application.controller.actions;

import application.controller.BasicController;
import application.controller.SavableController;
import application.controller.commands.exceptions.OpenFileException;
import application.model.collection.AbstractCollectionManager;
import application.model.collection.exceptions.CollectionException;
import application.model.data.exceptions.InvalidDataException;
import jdk.internal.org.xml.sax.SAXException;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

public class OpenFileAction implements Action {
    private final SavableController controller;
    private final String path;
    private final AbstractCollectionManager<?> collectionManager;

    public OpenFileAction(SavableController controller, String path, AbstractCollectionManager<?> collectionManager) {
        this.controller = controller;
        this.path = path;
        this.collectionManager = collectionManager;
    }

    @Override
    public void act() {
        try {
            File inputFile = new File(path);
            if (!inputFile.canRead()) throw new OpenFileException(path, "Can't read");
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            collectionManager.parse(doc.getDocumentElement());

            controller.setSavePath(path);

        } catch (ParserConfigurationException | IOException | InvalidDataException | CollectionException | org.xml.sax.SAXException e) {
            throw new OpenFileException(path, e.getMessage());
        }
    }
}
