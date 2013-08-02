/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.cardset.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.filechooser.FileFilter;

import forge.ImageCache;
import forge.cardset.CardSet;
import forge.cardset.CardSet;
import forge.item.PaperCard;
import forge.properties.NewConstants;
import forge.util.FileSection;
import forge.util.FileUtil;
import forge.util.IItemSerializer;
import forge.util.storage.StorageReaderFolder;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * This class knows how to make a file out of a cardset object and vice versa.
 */
public class CardSetSerializer extends StorageReaderFolder<CardSet> implements IItemSerializer<CardSet> {
    private final boolean moveWronglyNamedCardSets;
    public static final String FILE_EXTENSION = ".dck";

    public CardSetSerializer(final File cardsetDir0) {
        this(cardsetDir0, false);
    }

    public CardSetSerializer(final File cardsetDir0, boolean moveWrongCardSets) {
        super(cardsetDir0, CardSet.FN_NAME_SELECTOR);
        moveWronglyNamedCardSets = moveWrongCardSets;
    }

    /** Constant <code>DCKFileFilter</code>. */
    public static final FilenameFilter DCK_FILE_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(final File dir, final String name) {
            return name.endsWith(FILE_EXTENSION);
        }
    };
    /** The Constant DCK_FILTER. */
    public static final FileFilter DCK_FILTER = new FileFilter() {
        @Override
        public boolean accept(final File f) {
            return f.getName().endsWith(FILE_EXTENSION) || f.isDirectory();
        }

        @Override
        public String getDescription() {
            return "Simple CardSet File .dck";
        }
    };

    /** The Constant HTML_FILTER. */
    public static final FileFilter HTML_FILTER = new FileFilter() {
        @Override
        public boolean accept(final File f) {
            return f.getName().endsWith(".html") || f.isDirectory();
        }

        @Override
        public String getDescription() {
            return "Proxy File .html";
        }
    };

    /**
     * <p>
     * writeCardSet.
     * </p>
     * 
     * @param d
     *            a {@link forge.cardset.CardSet} object.
     * @param out
     *            a {@link java.io.BufferedWriter} object.
     * @throws java.io.IOException
     *             if any.
     */
    private static void writeCardSetHtml(final CardSet d, final BufferedWriter out) throws IOException {
        Template temp = null;
        final int cardBorder = 0;
        final int height = 319;
        final int width = 222;

        /* Create and adjust the configuration */
        final Configuration cfg = new Configuration();
        try {
            cfg.setClassForTemplateLoading(d.getClass(), "/");
            cfg.setObjectWrapper(new DefaultObjectWrapper());

            /*
             * ------------------------------------------------------------------
             * -
             */
            /*
             * You usually do these for many times in the application
             * life-cycle:
             */

            /* Get or create a template */
            temp = cfg.getTemplate("proxy-template.ftl");

            /* Create a data-model */
            /*final Map<String, Object> root = new HashMap<String, Object>();
            root.put("title", d.getName());
            final List<String> list = new ArrayList<String>();
            for (final Entry<PaperCard, Integer> card : d.getMain()) {
                // System.out.println(card.getSets().get(card.getSets().size() - 1).URL);
                for (int i = card.getValue().intValue(); i > 0; --i ) {
                    PaperCard r = card.getKey();
                    String url = NewConstants.URL_PIC_DOWNLOAD + ImageCache.getDownloadUrl(r, false);
                    list.add(url);
                }
            }

            final TreeMap<String, Integer> map = new TreeMap<String, Integer>();
            for (final Entry<PaperCard, Integer> entry : d.getMain().getOrderedList()) {
                map.put(entry.getKey().getName(), entry.getValue());
                // System.out.println(entry.getValue() + " " +
                // entry.getKey().getName());
            }

            root.put("urls", list);
            root.put("cardBorder", cardBorder);
            root.put("height", height);
            root.put("width", width);
            root.put("cardlistWidth", width - 11);
            root.put("cardList", map);*/

            /* Merge data-model with template */
            /*temp.process(root, out);*/
            out.flush();
        } catch (final IOException e) {
            System.out.println(e.toString());
        }/* catch (final TemplateException e) {
            System.out.println(e.toString());
        }*/
    }

    public static void writeCardSet(final CardSet d, final File f) {
        FileUtil.writeFile(f, d.save());
    }

    public static void writeCardSetHtml(final CardSet d, final File f) {
        try {
            final BufferedWriter writer = new BufferedWriter(new FileWriter(f));
            CardSetSerializer.writeCardSetHtml(d, writer);
            writer.close();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void save(final CardSet unit) {
        FileUtil.writeFile(this.makeFileFor(unit), unit.save());
    }

    @Override
    public void erase(final CardSet unit) {
        this.makeFileFor(unit).delete();
    }

    public File makeFileFor(final CardSet cardset) {
        return new File(this.getDirectory(), cardset.getBestFileName() + FILE_EXTENSION);
    }

    @Override
    protected CardSet read(final File file) {
        final Map<String, List<String>> sections = FileSection.parseSections(FileUtil.readFile(file));
        CardSet result = CardSet.fromSections(sections, true);

        if (moveWronglyNamedCardSets) {
            adjustFileLocation(file, result);
        }
        return result;
    }

    private void adjustFileLocation(final File file, final CardSet result) {
        if (result == null) {
            file.delete();
        } else {
            String destFilename = result.getBestFileName() + FILE_EXTENSION;
            if (!file.getName().equals(destFilename)) {
                file.renameTo(new File(file.getParentFile().getParentFile(), destFilename));
            }
        }
    }

    @Override
    protected FilenameFilter getFileFilter() {
        return CardSetSerializer.DCK_FILE_FILTER;
    }

    public static CardSetFileHeader readCardSetMetadata(final Map<String, List<String>> map, final boolean canThrow) {
        if (map == null) {
            return null;
        }
        final List<String> metadata = map.get("metadata");
        if (metadata != null) {
            return new CardSetFileHeader(FileSection.parse(metadata, "="));
        }
        return null;
    }

    /**
     * TODO: Write javadoc for this method.
     * @param model
     * @param filename
     */
    public static void writeCard(CardSet model, File filename) {
        // TODO Auto-generated method stub
        
    }

    /**
     * TODO: Write javadoc for this method.
     * @param model
     * @param filename
     */
    public static void writeCardHtml(CardSet model, File filename) {
        // TODO Auto-generated method stub
        
    }

    /**
     * TODO: Write javadoc for this method.
     * @param model
     * @param filename
     */
    public static void writeSet(CardSet model, File filename) {
        // TODO Auto-generated method stub
        
    }

    /**
     * TODO: Write javadoc for this method.
     * @param model
     * @param filename
     */
    public static void writeSetHtml(CardSet model, File filename) {
        // TODO Auto-generated method stub
        
    }
}
