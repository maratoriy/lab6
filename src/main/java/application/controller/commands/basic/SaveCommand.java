package application.controller.commands.basic;

import application.controller.BasicController;
import application.controller.SavableController;
import application.controller.commands.AbstractCommand;
import application.controller.commands.CommandParameters;
import application.controller.commands.exceptions.OpenFileException;
import application.model.collection.AbstractCollectionManager;
import application.model.collection.CollectionManager;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

public class SaveCommand extends AbstractCommand {
    private final AbstractCollectionManager<?> collectionManager;
    private final SavableController controller;

    public SaveCommand(SavableController controller, AbstractCollectionManager<?> collectionManager) {
        super("save", "save the collection");
        this.collectionManager = collectionManager;
        this.controller = controller;
    }

    @Override
    public void execute(CommandParameters params) {
        String savePath = controller.getSavePath();
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();

            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();

            Document document = documentBuilder.newDocument();

            document.appendChild(collectionManager.parse(document));

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);

            StringWriter result = new StringWriter();
            File file = new File(savePath);
            file.createNewFile();
            if (!file.canWrite()) throw new OpenFileException(savePath, "Can't write");

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            StreamResult streamResult = new StreamResult(result);
            transformer.transform(domSource, streamResult);
            bufferedWriter.write(result.toString());

            bufferedWriter.close();

        } catch (ParserConfigurationException | TransformerException | IOException e) {
            throw new OpenFileException(savePath, e.getMessage());
        }
    }
}
