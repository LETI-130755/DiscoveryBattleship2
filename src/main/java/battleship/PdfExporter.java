package battleship;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PdfExporter {

    private static final float MARGIN = 50;
    private static final float LINE_HEIGHT = 16;

    public static void export(IGame game, String file) throws IOException {
        List<String> lines = buildLines(game);

        try (PDDocument doc = new PDDocument()) {
            PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDPage page = new PDPage();
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float y = page.getMediaBox().getHeight() - MARGIN;

            for (String line : lines) {
                if (y < MARGIN) {                       // nova página quando acaba o espaço
                    cs.close();
                    page = new PDPage();
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = page.getMediaBox().getHeight() - MARGIN;
                }
                cs.beginText();
                cs.setFont(font, 12);
                cs.newLineAtOffset(MARGIN, y);
                cs.showText(line);
                cs.endText();
                y -= LINE_HEIGHT;
            }
            cs.close();
            doc.save(file);
        }
    }

    private static List<String> buildLines(IGame game) {
        List<String> lines = new ArrayList<>();
        lines.add("Battleship - Registo de jogadas");
        lines.add("");
        for (IMove move : game.getAlienMoves()) {
            StringBuilder shots = new StringBuilder("Rajada " + move.getNumber() + ": ");
            for (IPosition p : move.getShots()) {
                shots.append(p).append("  ");            // toString() devolve ex.: B3
            }
            lines.add(shots.toString());
            for (IGame.ShotResult r : move.getShotResults()) {
                if (r.repeated())          lines.add("    tiro repetido");
                else if (!r.valid())       lines.add("    tiro fora do tabuleiro");
                else if (r.ship() == null) lines.add("    Agua");
                else lines.add("    " + r.ship().getCategory() + (r.sunk() ? " afundado" : " atingido"));
            }
        }
        lines.add("");
        lines.add("Acertos: " + game.getHits() + "   Afundados: " + game.getSunkShips()
                + "   Repetidos: " + game.getRepeatedShots() + "   Fora: " + game.getInvalidShots());
        lines.add("Navios restantes: " + game.getRemainingShips());
        return lines;
    }
}