<%@ page import="
	java.util.*,
	org.opencms.jsp.*,
	org.opencms.main.*,
	org.opencms.util.*,
	org.opencms.workplace.editors.*,
	org.opencms.editors.ckeditor.*
"%><%

CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
CmsCKEditor wp = new CmsCKEditor(cms);

CmsRequestUtil.setNoCacheHeaders(response);

CmsEditorDisplayOptions options = OpenCms.getWorkplaceManager().getEditorDisplayOptions();
Properties displayOptions = options.getDisplayOptions(cms);

// get editor configuration object from session, because request parameters do not work
CmsCKEditorConfiguration extConf = CmsCKEditorConfiguration.getConfiguration(session);

%>CKEDITOR.editorConfig = function( config )
{
<%

String cssPath = extConf.getUriStyleSheet();

// This editor supports user defined styles. To show these styles, a plain text file containing the style definition
// XML code has to be placed in the same folder where the template CSS style sheet is located.
// The file name has to be exactly like the file name of the CSS with the suffix "_style.xml" added.
// E.g. for the CSS file "style.css" the style definition file has to be named "style.css_style.xml".
// An example for a style XML can be found in the VFS file "/system/workplace/resources/editors/fckeditor/fckstyles.xml".
boolean styleXMLPresent = false;
if (CmsStringUtil.isNotEmpty(cssPath)) {
	String pathUsed = cssPath;
	int idx = pathUsed.indexOf('?');
	if (idx != -1) {
		pathUsed = cssPath.substring(0, idx);
	}
	String styleXML = pathUsed + CmsCKEditor.SUFFIX_STYLESXML;
	if (cms.getCmsObject().existsResource(styleXML)) {
		styleXMLPresent = true;
		%>config.stylesCombo_stylesSet = "mystyles:<%= cms.link(styleXML) %>",<%
	}
	%>config.contentsCSS = "<%= cms.link(cssPath) %>",<%
}

String resource = extConf.getResourcePath();

String site = OpenCms.getSiteManager().getWorkplaceServer();

%>
config.language = "<%= wp.getLocale().getLanguage() %>",
config.defaultLanguage = '<%= Locale.ENGLISH %>',

config.width = "100%",
config.height = "400px",
config.resize_enabled = false,

config.enterMode = CKEDITOR.ENTER_P,
config.shiftEnterMode = CKEDITOR.ENTER_BR,

config.entities = true,
config.entities_processNumerical = false,
config.entities_latin = false,
config.entities_greek = false,

config.pasteFromWordIgnoreFontFace = true,
config.pasteFromWordKeepsStructure = false,
config.pasteFromWordRemoveStyle = true,

<%
String formatOptions = options.getOptionValue("formatselect.options", "", displayOptions);
if (CmsStringUtil.isNotEmpty(formatOptions)) { %>
config.format_tags = "<%= formatOptions %>",<%
} %>

config.baseHref = "<%= site %>",
config.toolbarCanCollapse = false,

config.skin = "opencms,<%= wp.getEditorResourceUri() %>skins/opencms/",

// overwrite default menu groups because of image context menu (replace "image" by "imageocms")
config.menu_groups = "clipboard,form,tablecell,tablecellproperties,tablerow,tablecolumn,table,anchor,link,imageocms,flash," +
	"checkbox,radio,textfield,hiddenfield,imagebutton,button,select,textarea",

<%

// remove unneeded plugins
String removePlugins = "image,elementspath,save,resize";
boolean showTableOptions = options.showElement("option.table", displayOptions);
if (!showTableOptions) {
	// remove table commands if the user does not have the permission to edit tables
	removePlugins = "table,tabletools," + removePlugins;
}
%>config.removePlugins = "<%= removePlugins %>",
config.extraPlugins = "opencms,iframedialog,imagegallery,downloadgallery,linkgallery,htmlgallery,tablegallery",<%

StringBuffer toolbar = new StringBuffer(2048);

toolbar.append("[");

if (CmsStringUtil.isNotEmpty(resource) && options.showElement("button.customized", displayOptions)) {
	I_CmsEditorActionHandler actionClass = OpenCms.getWorkplaceManager().getEditorActionHandler();
	if (actionClass.isButtonActive(wp.getJsp(), resource)) {
		toolbar.append("'oc-publish',");
	}
}

toolbar.append("'oc-save_exit','oc-save'");

// source code button
if (options.showElement("option.sourcecode", displayOptions)) {
	toolbar.append(",'-','Source'");
}

// standard buttons: undo/redo, find, cut/copy/paste
toolbar.append(",'-','Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat','-','Cut','Copy','Paste','PasteText','PasteFromWord'");


// determine if the insert table button should be shown
if (showTableOptions) {
	toolbar.append(",'-','Table'");
	// toolbar.append(",'-','TableInsertRow','TableDeleteRows','TableInsertColumn','TableDeleteColumns','TableInsertCell','TableDeleteCells','TableMergeCells','TableSplitCell'");
}

// determine if the insert link buttons should be shown
if (options.showElement("option.links", displayOptions)) {
	toolbar.append(",'-'");

	// determine if the local link button should be shown
	if (options.showElement("option.link", displayOptions)) {
		toolbar.append(",'oc-link'");
	}

	// determine if the external link button should be shown
	if (options.showElement("option.extlink", displayOptions)) {
		toolbar.append(",'Link'");
	}

	// determine if the anchor button should be shown
	if (options.showElement("option.anchor", displayOptions)) {
		toolbar.append(",'Anchor'");
	}

	// determine if the unlink buttons should be shown
	if (options.showElement("option.unlink", displayOptions)) {
		toolbar.append(",'Unlink'");
	}

}

// determine if the flash button button should be shown
if (options.showElement("option.flash", displayOptions)) {
	toolbar.append(",'-','Flash'");
}

// determine if the insert/edit image button should be shown
if (options.showElement("option.images", displayOptions) || options.showElement("gallery.image", displayOptions)) {
	// replaced by image gallery: toolbar.append(",'-', 'OcmsImage'");
	toolbar.append(",'-', 'OcmsImageGallery'");
}

if (options.showElement("gallery.download", displayOptions)) {
	toolbar.append(", 'OcmsDownloadGallery'");
}

if (options.showElement("gallery.link", displayOptions)) {
	toolbar.append(", 'OcmsLinkGallery'");
}

if (options.showElement("gallery.html", displayOptions)) {
	toolbar.append(", 'OcmsHtmlGallery'");
}

if (options.showElement("gallery.table", displayOptions)) {
	toolbar.append(", 'OcmsTableGallery'");
}

boolean showRule = options.showElement("option.rule", displayOptions);
boolean showSpecial = options.showElement("option.specialchars", displayOptions);

if (showRule || showSpecial) {

	toolbar.append(",'-'");
	// insert horizontal rule button
	if (showRule) {
		toolbar.append(",'HorizontalRule'");
	}

	// determine if the insert special characters button should be shown
	if (showSpecial) {
		toolbar.append(",'SpecialChar'");
	}
}

// determine if the print button should be shown
if (options.showElement("option.print", displayOptions)) {
	toolbar.append(",'-','Print'");
}

// determine if the spell check button should be shown
if (options.showElement("option.spellcheck", displayOptions)) {
	toolbar.append(",'-','SpellChecker'");
}

// determine if the help button should be shown
if (wp.isHelpEnabled()) {
	if (options.showElement("option.help", displayOptions)) {
		toolbar.append(",'-','oc-help'");
	}
}

toolbar.append(",'-','oc-exit']");

// style buttons
boolean fontFormat = options.showElement("option.formatselect", CmsStringUtil.TRUE, displayOptions);
boolean fontFace = options.showElement("font.face", displayOptions);
boolean fontSize = options.showElement("font.size", displayOptions);
boolean style = styleXMLPresent && options.showElement("option.style", displayOptions);

StringBuffer stylebar = new StringBuffer(1536);

// determine if the font format selector should be shown
if (fontFormat) {
	stylebar.append("'Format'");
}
// determine if the font face selector should be shown
if (fontFace) {
	stylebar.append(",'Font'");
}

// determine if the font size selector should be shown
if (fontSize) {
	stylebar.append(",'FontSize'");
}


// determine if the style selector should be shown
if (style) {
	stylebar.append(",'Styles'");
}

boolean showStyleBt = false;
boolean showScriptBt = false;

// determine if the font decoration buttons should be shown
if (options.showElement("font.decoration", displayOptions)) {
	if (options.showElement("button.bold", CmsStringUtil.TRUE, displayOptions)) {
		stylebar.append(",'Bold'");
		showStyleBt = true;
	}
	if (options.showElement("button.italic", CmsStringUtil.TRUE, displayOptions)) {
		stylebar.append(",'Italic'");
		showStyleBt = true;
	}
	if (options.showElement("button.underline", CmsStringUtil.TRUE, displayOptions)) {
		stylebar.append(",'Underline'");
		showStyleBt = true;
	}
	if (options.showElement("button.strikethrough", CmsStringUtil.TRUE, displayOptions)) {
		stylebar.append(",'Strike'");
		showStyleBt = true;
	}

	StringBuffer styleBt = new StringBuffer(32);
	if (options.showElement("button.sub", CmsStringUtil.TRUE, displayOptions)) {
		showScriptBt = true;
		styleBt.append(",'Subscript'");
	}
	if (options.showElement("button.super", CmsStringUtil.TRUE, displayOptions)) {
		showScriptBt = true;
		styleBt.append(",'Superscript'");
	}
        if (showScriptBt) {
        	if (showStyleBt) {
			// append leading separator in case format buttons were rendered
			stylebar.append(",'-'");
		}
		stylebar.append(styleBt);
        }
}

boolean showAlignBt = false;

// determine if the text alignment buttons should be shown
if (options.showElement("text.align", displayOptions)) {
	StringBuffer alignBt = new StringBuffer(64);
	if (options.showElement("button.alignleft", CmsStringUtil.TRUE, displayOptions)) {
		showAlignBt = true;
		alignBt.append(",'JustifyLeft'");
	}
	if (options.showElement("button.aligncenter", CmsStringUtil.TRUE, displayOptions)) {
		showAlignBt = true;
		alignBt.append(",'JustifyCenter'");
	}
	if (options.showElement("button.alignright", CmsStringUtil.TRUE, displayOptions)) {
		showAlignBt = true;
		alignBt.append(",'JustifyRight'");
	}
	if (options.showElement("button.justify", CmsStringUtil.TRUE, displayOptions)) {
		showAlignBt = true;
		alignBt.append(",'JustifyBlock'");
	}
	if (showAlignBt) {
		if (showScriptBt || showStyleBt) {
			stylebar.append(",'-'");
		}
		stylebar.append(alignBt);
	}

}

boolean showListBt = false;

// determine if the text list buttons should be shown
if (options.showElement("text.lists", displayOptions)) {
	StringBuffer listBt = new StringBuffer(32);
	if (options.showElement("button.orderedlist", CmsStringUtil.TRUE, displayOptions)) {
		showListBt = true;
		listBt.append(",'NumberedList'");
	}
	if (options.showElement("button.unorderedlist", CmsStringUtil.TRUE, displayOptions)) {
		showListBt = true;
		listBt.append(",'BulletedList'");
	}
	if (showListBt) {
		if (showScriptBt || showStyleBt || showAlignBt) {
			stylebar.append(",'-'");
		}
		stylebar.append(listBt);
	}
}

// determine if the text indentation buttons should be shown
if (options.showElement("text.indent", displayOptions)) {
	boolean showIndBt = false;
	StringBuffer indBt = new StringBuffer(32);
	if (options.showElement("button.outdent", CmsStringUtil.TRUE, displayOptions)) {
		showIndBt = true;
		indBt.append(",'Outdent'");
	}
	if (options.showElement("button.indent", CmsStringUtil.TRUE, displayOptions)) {
		showIndBt = true;
		indBt.append(",'Indent'");
	}
	if (showIndBt) {
		if (showScriptBt || showStyleBt || showAlignBt || showListBt) {
			stylebar.append(",'-'");
		}
		stylebar.append(indBt);
	}
}

// determine which color selectors should be shown
boolean fontColor = options.showElement("font.color", displayOptions);
boolean bgColor = options.showElement("bg.color", displayOptions);
if (fontColor || bgColor) {
%>config.colorButton_enableMore = true,<%
    stylebar.append(",'-',");
    if (fontColor && bgColor) {
      stylebar.append("'TextColor','BGColor'");
    } else {
    	if (fontColor) {
           stylebar.append("'TextColor'");
        }
        if (bgColor) {
           stylebar.append("'BGColor'");
        }
    }
}

if (stylebar.length() > 0) {
	String styleStr = stylebar.toString();
	if (styleStr.charAt(0) == ',') {
		styleStr = styleStr.substring(1);
	}
	toolbar.append(", '/'\n,[");
	toolbar.append(styleStr);
	toolbar.append("]");
}

// determines if the form editing buttons should be shown
if (options.showElement("option.form", displayOptions)) {
        toolbar.append(",['Form','-','Checkbox','Radio','TextField','Textarea','Select','Button','ImageButton','HiddenField']");
}

%>

config.toolbar = [
        <%= toolbar %>
],

config.keystrokes = [
	[ CKEDITOR.CTRL + 65 /*A*/, true ],
	[ CKEDITOR.CTRL + 67 /*C*/, true ],
	[ CKEDITOR.CTRL + 70 /*F*/, true ],
	[ CKEDITOR.CTRL + 83 /*S*/, 'oc-save' ],
	[ CKEDITOR.CTRL + 88 /*X*/, true ],
	[ CKEDITOR.CTRL + 86 /*V*/, 'paste' ],
	[ CKEDITOR.SHIFT + 45 /*INS*/, 'paste' ],
	[ CKEDITOR.CTRL + 90 /*Z*/, 'undo' ],
	[ CKEDITOR.CTRL + 89 /*Y*/, 'redo' ],
	[ CKEDITOR.CTRL + CKEDITOR.SHIFT + 90 /*Z*/, 'redo' ],
	[ CKEDITOR.CTRL + 76 /*L*/, 'link' ],
	[ CKEDITOR.CTRL + 66 /*B*/, 'bold' ],
	[ CKEDITOR.CTRL + 73 /*I*/, 'italic' ],
	[ CKEDITOR.CTRL + 85 /*U*/, 'underline' ],
	[ CKEDITOR.CTRL + CKEDITOR.SHIFT + 88 /*X*/, 'oc-exit' ],
	[ CKEDITOR.CTRL + CKEDITOR.SHIFT + 83 /*S*/, 'oc-save_exit' ],
	[ CKEDITOR.CTRL + CKEDITOR.ALT + 13 /*ENTER*/, 'fitWindow' ]<%
	if (options.showElement("option.sourcecode", displayOptions)) { %>,
	[ CKEDITOR.CTRL + 9 /*TAB*/, 'source' ]<%
	} %>
]

};