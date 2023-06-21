package com.folioreader.util

import com.folioreader.model.media_overlay.OverlayItems
import org.w3c.dom.Element
import org.w3c.dom.Node

/**
 * @author gautam chibde on 20/6/17.
 */
object SMILParser {
    /**
     * Function creates list [OverlayItems] of all tag elements from the
     * input html raw string.
     *
     * @param html raw html string
     * @return list of [OverlayItems]
     */
    //    public static List<OverlayItems> parseSMIL(String html) {
    //        List<OverlayItems> mediaItems = new ArrayList<>();
    //        try {
    //            Document document = EpubParser.xmlParser(html);
    //            NodeList sections = document.getDocumentElement().getElementsByTagName("section");
    //            for (int i = 0; i < sections.getLength(); i++) {
    //                parseNodes(mediaItems, (Element) sections.item(i));
    //            }
    //        } catch (Exception e) {
    //            return new ArrayList<>();
    //        }
    //        return mediaItems;
    //    }
    /**
     * [RECURSIVE]
     * Function recursively finds and parses the child elements of the input
     * DOM element.
     *
     * @param names   input [OverlayItems] where data is to be stored
     * @param section input DOM element
     */
    private fun parseNodes(names: MutableList<OverlayItems>, section: Element) {
        var n = section.firstChild
        while (n != null) {
            if (n.nodeType == Node.ELEMENT_NODE) {
                val e = n as Element
                if (e.hasAttribute("id")) {
                    names.add(OverlayItems(e.getAttribute("id"), e.tagName))
                } else {
                    parseNodes(names, e)
                }
            }
            n = n.nextSibling
        }
    }
    /**
     * function finds all the text content inside input html page and splits each sentence
     * with separator '.' and returns them as a list of [OverlayItems]
     *
     * @param html input raw html
     * @return generated [OverlayItems]
     */
    //    public static List<OverlayItems> parseSMILForTTS(String html) {
    //        List<OverlayItems> mediaItems = new ArrayList<>();
    //        try {
    //            Document document = null; //EpubParser.xmlParser(html);
    //            NodeList sections = document.getDocumentElement().getElementsByTagName("body");
    //            for (int i = 0; i < sections.getLength(); i++) {
    //                parseNodesTTS(mediaItems, (Element) sections.item(i));
    //            }
    //            //} catch (EpubParserException e) {
    //        } catch (Exception e) {
    //            return new ArrayList<>();
    //        }
    //        return mediaItems;
    //    }
    /**
     * [RECURSIVE]
     * Function recursively looks for the child element with the text content and
     * adds them to the input [OverlayItems] list
     *
     * @param names   input [OverlayItems] where data is to be stored
     * @param section input DOM element
     */
    private fun parseNodesTTS(names: MutableList<OverlayItems>, section: Element) {
        var n = section.firstChild
        while (n != null) {
            if (n.nodeType == Node.ELEMENT_NODE) {
                val e = n as Element
                var n1 = e.firstChild
                while (n1 != null) {
                    if (n1.textContent != null) {
                        for (s in n1.textContent.split("\\.".toRegex())
                            .dropLastWhile { it.isEmpty() }
                            .toTypedArray()) {
                            if (!s.isEmpty()) {
                                val i = OverlayItems()
                                i.text = s
                                names.add(i)
                            }
                        }
                    }
                    n1 = n1.nextSibling
                }
                parseNodesTTS(names, e)
            }
            n = n.nextSibling
        }
    }
}