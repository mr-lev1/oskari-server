package fi.nls.oskari.myplaces.service.wfst;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import fi.nls.oskari.domain.map.UserDataStyle;
import org.oskari.wfst.WFSTRequestBuilder;

import fi.nls.oskari.domain.map.MyPlaceCategory;

public class CategoriesWFSTRequestBuilder extends WFSTRequestBuilder {

    private static final String TYPENAME_CATEGORIES = "feature:categories";
    protected static final String APPLICATION_JSON = "application/json";

    public static void getCategoryById(OutputStream out, long categoryId)
            throws XMLStreamException {
        XMLStreamWriter xsw = XOF.createXMLStreamWriter(out);
        writeGetFeature(xsw, "1.1.0");
        xsw.writeAttribute("outputFormat", APPLICATION_JSON);

        xsw.writeStartElement(WFS, "Query");
        xsw.writeAttribute("typeName", TYPENAME_CATEGORIES);
        writeFeatureIdFilter(xsw, prefixId(categoryId));
        xsw.writeEndElement();

        xsw.writeEndElement();
        xsw.writeEndDocument();
        xsw.close();
    }

    public static void getCategoriesById(OutputStream out, long[] categoryIds)
            throws XMLStreamException {
        XMLStreamWriter xsw = XOF.createXMLStreamWriter(out);
        writeGetFeature(xsw, "1.1.0");
        xsw.writeAttribute("outputFormat", APPLICATION_JSON);

        xsw.writeStartElement(WFS, "Query");
        xsw.writeAttribute("typeName", TYPENAME_CATEGORIES);
        for (long categoryId : categoryIds) {
            writeFeatureIdFilter(xsw, prefixId(categoryId));
        }
        xsw.writeEndElement();

        xsw.writeEndElement();
        xsw.writeEndDocument();
        xsw.close();
    }

    public static void getCategoriesByUuid(OutputStream out, String uuid)
            throws XMLStreamException {
        XMLStreamWriter xsw = XOF.createXMLStreamWriter(out);
        writeGetFeature(xsw, "1.1.0");
        xsw.writeAttribute("outputFormat", APPLICATION_JSON);

        xsw.writeStartElement(WFS, "Query");
        xsw.writeAttribute("typeName", TYPENAME_CATEGORIES);

        xsw.writeStartElement(OGC, "Filter");
        xsw.writeStartElement(OGC, "PropertyIsEqualTo");
        writeTextElement(xsw, OGC, "PropertyName", "uuid");
        writeTextElement(xsw, OGC, "Literal", uuid);
        xsw.writeEndElement();
        xsw.writeEndElement();

        xsw.writeEndElement(); // <wfs:Query>

        xsw.writeEndElement(); // <wfs:GetFeature>
        xsw.writeEndDocument();
        xsw.close();
    }

    public static String[] insertCategories(OutputStream out, List<MyPlaceCategory> categories)
            throws XMLStreamException, IOException {
        XMLStreamWriter xsw = XOF.createXMLStreamWriter(out);
        writeStartTransaction(xsw, "1.1.0");

        final int n = categories.size();
        String[] handles = new String[n];
        for (int i = 0; i < n; i++) {
            String handle = "c" + i;
            handles[i] = handle;
            MyPlaceCategory category = categories.get(i);
            insertCategory(xsw, category, handle);
        }

        xsw.writeEndElement();
        xsw.writeEndDocument();
        xsw.close();

        return handles;
    }

    public static void updateCategories(OutputStream out, List<MyPlaceCategory> categories)
            throws XMLStreamException, IOException {
        XMLStreamWriter xsw = XOF.createXMLStreamWriter(out);
        writeStartTransaction(xsw, "1.1.0");
        for (MyPlaceCategory category : categories) {
            updateCategory(xsw, category);
        }
        xsw.writeEndElement();
        xsw.writeEndDocument();
        xsw.close();
    }

    public static void deleteCategories(OutputStream out, long[] categoryIds)
            throws XMLStreamException {
        XMLStreamWriter xsw = XOF.createXMLStreamWriter(out);
        writeStartTransaction(xsw, "1.1.0");
        for (long categoryId : categoryIds) {
            deleteCategory(xsw, categoryId);
        }
        xsw.writeEndElement();
        xsw.writeEndDocument();
        xsw.close();
    }

    private static void insertCategory(XMLStreamWriter xsw,
            MyPlaceCategory category, String handle) throws XMLStreamException {
        UserDataStyle style = category.getStyle();
        xsw.writeStartElement(WFS, "Insert");
        xsw.writeAttribute("typeName", TYPENAME_CATEGORIES);
        if (handle != null && !handle.isEmpty()) {
            xsw.writeAttribute("handle", handle);
        }

        xsw.writeStartElement(OSKARI, "categories");

        writeTextElement(xsw, OSKARI, "default", Boolean.toString(category.isDefault()));
        writeTextElement(xsw, OSKARI, "category_name", category.getCategory_name());
        writeTextElement(xsw, OSKARI, "uuid", category.getUuid());
        writeTextElement(xsw, OSKARI, "publisher_name", category.getPublisher_name());
        // STYLE
        writeTextElement(xsw, OSKARI, "stroke_width", style.getStroke_width());
        writeTextElement(xsw, OSKARI, "stroke_color", style.getStroke_color());
        writeTextElement(xsw, OSKARI, "fill_color", style.getFill_color());
        writeTextElement(xsw, OSKARI, "dot_color", style.getDot_color());
        writeTextElement(xsw, OSKARI, "dot_size", style.getDot_size());
        writeTextElement(xsw, OSKARI, "border_width", style.getBorder_width());
        writeTextElement(xsw, OSKARI, "border_color", style.getBorder_color());
        writeTextElement(xsw, OSKARI, "dot_shape", style.getDot_shape());
        writeTextElement(xsw, OSKARI, "stroke_linejoin", style.getStroke_linejoin());
        writeTextElement(xsw, OSKARI, "fill_pattern", style.getFill_pattern());
        writeTextElement(xsw, OSKARI, "stroke_linecap", style.getStroke_linecap());
        writeTextElement(xsw, OSKARI, "stroke_dasharray", style.getStroke_dasharray());
        writeTextElement(xsw, OSKARI, "border_linejoin", style.getBorder_linejoin());
        writeTextElement(xsw, OSKARI, "border_dasharray", style.getBorder_dasharray());

        xsw.writeEndElement(); // Close <feature:categories>
        xsw.writeEndElement(); // Close <wfs:Insert>
    }

    private static void updateCategory(XMLStreamWriter xsw, MyPlaceCategory category)
            throws XMLStreamException {
        UserDataStyle style = category.getStyle();
        xsw.writeStartElement(WFS, "Update");
        xsw.writeAttribute("typeName", TYPENAME_CATEGORIES);

        writeProperty(xsw, "default", Boolean.toString(category.isDefault()));
        writeProperty(xsw, "category_name", category.getCategory_name());
        writeProperty(xsw, "publisher_name", category.getPublisher_name());
        writeProperty(xsw, "uuid", category.getUuid());
        // STYLE
        writeProperty(xsw, "stroke_width", style.getStroke_width());
        writeProperty(xsw, "stroke_color", style.getStroke_color());
        writeProperty(xsw, "fill_color", style.getFill_color());
        writeProperty(xsw, "dot_color", style.getDot_color());
        writeProperty(xsw, "dot_size", style.getDot_size());
        writeProperty(xsw, "border_width", style.getBorder_width());
        writeProperty(xsw, "border_color", style.getBorder_color());
        writeProperty(xsw, "dot_shape", style.getDot_shape());
        writeProperty(xsw, "stroke_linejoin", style.getStroke_linejoin());
        writeProperty(xsw, "fill_pattern", style.getFill_pattern());
        writeProperty(xsw, "stroke_linecap", style.getStroke_linecap());
        writeProperty(xsw, "stroke_dasharray", style.getStroke_dasharray());
        writeProperty(xsw, "border_linejoin", style.getBorder_linejoin());
        writeProperty(xsw, "border_dasharray", style.getBorder_dasharray());

        writeFeatureIdFilter(xsw, prefixId(category.getId()));

        xsw.writeEndElement(); // close <wfs:Update>
    }

    private static void deleteCategory(XMLStreamWriter xsw, long id)
            throws XMLStreamException {
        xsw.writeStartElement(WFS, "Delete");
        xsw.writeAttribute("typeName", TYPENAME_CATEGORIES);
        writeFeatureIdFilter(xsw, prefixId(id));
        xsw.writeEndElement();
    }

    public static String prefixId(long id) {
        return "categories." + id;
    }

    public static String[] prefixIds(long[] ids) {
        final int n = ids.length;
        String[] prefixed = new String[n];
        for (int i = 0; i < n; i++) {
            prefixed[i] = prefixId(ids[i]);
        }
        return prefixed;
    }

    public static long removePrefixFromId(String prefixed) {
        return Long.parseLong(prefixed.substring("categories.".length()));
    }

}
