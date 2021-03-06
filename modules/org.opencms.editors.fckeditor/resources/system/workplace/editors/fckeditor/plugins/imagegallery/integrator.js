<%@ page import="org.opencms.jsp.*" %><%
CmsJspActionElement cms = new CmsJspActionElement(pageContext, request, response);
%><%= cms.getContent("/system/workplace/resources/editors/fckeditor/editor/dialog/common/fck_dialog_common.js") %>

/* Initialize important FCKeditor variables from editor. */
var dialog		= window.parent;
var oEditor		= dialog.InnerDialogLoaded();
var FCK			= oEditor.FCK;
var FCKConfig		= oEditor.FCKConfig;
var FCKBrowserInfo	= oEditor.FCKBrowserInfo;

/* Enables or disables the enhanced image dialog options. */
var showEnhancedOptions = FCKConfig.ShowEnhancedOptions;
var useTbForLinkOriginal = FCKConfig.UseTbForLinkOriginal;

/* The selected image (if available). */
var oImage = null;

/* The active link. */
var oLink = null;

/* The span around the image, if present. */
var oSpan = null;

/* Absolute path to the JSP that displays the image in original size. */
var vfsPopupUri = "<%= cms.link("/system/workplace/editors/fckeditor/plugins/ocmsimage/popup.html") %>";

/* Do additional stuff when active image is loaded. */
/**
 * Sets values in the gallery preview.
 * 
 * Additional values like copyright, title/alt
 */
function activeImageAdditionalActions(isInitial) {
	// 
    if (isInitial) {
        loadSelection();
    } else {
    	// showEnhancedOptions = true 
        if (isEnhanced()) {
            resetCopyrightText();
        }
        // title/alt attribute
        var imgTitle = cms.galleries.activeItem.title;
        if (cms.galleries.activeItem.description != "") {
            imgTitle = cms.galleries.activeItem.description;
        }
        GetE("txtAlt").value = imgTitle;
        
        //set default values for margin and alignment for the image opened from the list
        $('#' + cms.imagepreviewhandler.keys['editorFormatTabId'])
            .find('#' + cms.imagepreviewhandler.editorKeys['imgAlign'])
            .find('.cms-selectbox').selectBox('setValue','left');        
		GetE("txtHSpace").value = "5";
		GetE("txtVSpace").value = "5";
        cms.galleries.checkGalleryCheckbox($('#' + cms.imagepreviewhandler.editorKeys['imgSpacing']),true);        
    }
	// activate the "OK" button of the fck dialog
	window.parent.SetOkButton(true);
}

/* Opens the file browser popup for the link dialog. */
function LnkBrowseServer() {
        OpenFileBrowser(FCKConfig.LinkBrowserURL, FCKConfig.LinkBrowserWindowWidth, FCKConfig.LinkBrowserWindowHeight);
}

/* Triggered by the file browser popup to set the selected URL in the input field. */
function SetUrl( url, width, height, alt ) {
        GetE("txtLnkUrl").value = url;
}

/**
 * Reads the attributes and values of the editor before the gallery is opened.
 * @return
 */
// TODO: move to fckplugin.js
function prepareEditor() {
    // get the selected image
	oImage = dialog.Selection.GetSelectedElement();
	// only img or span or input(type=img)
	if (oImage && oImage.tagName != "IMG" && oImage.tagName != "SPAN" && !(oImage.tagName == "INPUT" && oImage.type == "image")) {
		oImage = null;
	}
	// get the active link
	oLink = dialog.Selection.GetSelection().MoveToAncestorNode("A");

	if (!oImage) {       
		return;
	}
    
	// fck url attribute
    var sUrl = oImage.getAttribute("_fcksavedurl");
	if (sUrl == null) {
		sUrl = GetAttribute(oImage, "src", "");
	}
	
	// read scale/cropping params	
	var paramIndex = sUrl.indexOf("?__scale");
	if (paramIndex != -1) {
		// set scale params global to gallery
		cms.galleries.initValues.scale = sUrl.substring(paramIndex + 9);
		sUrl = sUrl.substring(0, paramIndex);
	}

	// set linkpath global to gallery
	cms.galleries.initValues.linkpath = sUrl;
    
	// read width and height
    var iWidth, iHeight;

	var regexSize = /^\s*(\d+)px\s*$/i ;

	// from style attribute
	if (oImage.style.width)	{
		var aMatch = oImage.style.width.match(regexSize);
		if (aMatch) {
			iWidth = aMatch[1];
			oImage.style.width = "";
		}
	}

	if (oImage.style.height) {
		var aMatch = oImage.style.height.match(regexSize);
		if (aMatch) {
			iHeight = aMatch[1];
			oImage.style.height = "";
		}
	}

	// else from width and height attribute 
	iWidth = iWidth ? iWidth : GetAttribute(oImage, "width", "");
	iHeight = iHeight ? iHeight : GetAttribute(oImage, "height", "");

	// set width and height global to gallery
	cms.galleries.initValues.imgwidth = "" + iWidth;
	cms.galleries.initValues.imgheight = "" + iHeight;

}

/**
 * Called when an image is selected in the editor and gallery is opened.<p>
 * 
 * Loads the available attributes and properties for the selected image from the editor.
 * Sets these values to the gallery tabs (editor formats tab, extended tab)
 */
// TODO: oImage and oLink are global varibales, both are set in the prepareEditor()
function loadSelection() {
	var altText = "";
	var copyText = "";
	var imgBorder =	false;
	var imgHSp = "";
	var imgVSp = "";
	var imgAlign = GetAttribute(oImage, "align", "");
	// if img is sourounded with 'span' or 'table'
	if (dialog.Selection.GetSelection().HasAncestorNode("SPAN") || dialog.Selection.GetSelection().HasAncestorNode("TABLE")) {
		if (FCK.Selection.HasAncestorNode("SPAN")) {
			oSpan =	dialog.Selection.GetSelection().MoveToAncestorNode("SPAN");
		} else {
			oSpan =	dialog.Selection.GetSelection().MoveToAncestorNode("TABLE");
		}
		try {
			var idPart = oSpan.getAttribute("id").substring(1);
			if (idPart == oImage.getAttribute("id").substring(1)) {
				// reading ext. subtitle
				var altElem = oEditor.FCK.EditorDocument.getElementById("s" + idPart);
				if (altElem) {
					altText	= altElem.firstChild.data;
                    cms.galleries.checkGalleryCheckbox($('#' + cms.imagepreviewhandler.editorKeys['fckInsertAlt']), true);                   
				}
				// reading ext. copyright
				var cpElem = oEditor.FCK.EditorDocument.getElementById("c" + idPart);
				if (cpElem) {
					copyText = cpElem.firstChild.data;
                    cms.galleries.checkGalleryCheckbox($('#' + cms.imagepreviewhandler.editorKeys['fckInsertCr']), true);					
				}
				// read margins
				var divElem = oEditor.FCK.EditorDocument.getElementById("a" + idPart);
				imgHSp = divElem.style.marginLeft;
				if (imgAlign == "left") {
			 		imgHSp = divElem.style.marginRight;
			 	} else if (imgAlign == "right") {
			 		imgHSp = divElem.style.marginLeft;
			 	}
				imgVSp = divElem.style.marginBottom;
			}
		} catch	(e) {}
	 } else	{  // only img tag
	 	if (imgAlign == "left") {
	 		// read margins or spacing
	 		imgHSp = oImage.style.marginRight;
			imgVSp = oImage.style.marginBottom;
			if (imgHSp == "") {
				imgHSp = GetAttribute(oImage, "hspace", "");
			}
			if (imgVSp == "") {
				imgVSp = GetAttribute(oImage, "vspace", "");
			}
	    // reading align attribute
	 	} else if (imgAlign == "right") {
	 		imgHSp = oImage.style.marginLeft;
	 		imgVSp = oImage.style.marginBottom;
	 		if (imgHSp == "") {
				imgHSp = GetAttribute(oImage, "hspace", "");
			}
			if (imgVSp == "") {
				imgVSp = GetAttribute(oImage, "vspace", "");
			}
	 	} else {
			imgHSp = GetAttribute(oImage, "hspace", "");
			imgVSp = GetAttribute(oImage, "vspace", "");
		}
	}
	
	// replace margins, if align attribute is set
	var cssTxt = oImage.style.cssText;	
	if (showEnhancedOptions) {
		if (imgAlign == "left") {
			cssTxt = cssTxt.replace(/margin-right:\s*\d+px;/, "");
			cssTxt = cssTxt.replace(/margin-bottom:\s*\d+px;/, "");
	 	} else if (imgAlign == "right") {
			cssTxt = cssTxt.replace(/margin-left:\s*\d+px;/, "");
			cssTxt = cssTxt.replace(/margin-bottom:\s*\d+px;/, "");
	 	}
 	}
	
	if (altText == "") {
		// reading alt attribute
		altText	= GetAttribute(oImage,	"alt", "");
	}
    
    // at this point the url is already given
    var sUrl = cms.galleries.initValues.linkpath;
    
    ////// Set the read attributes to gallery input fields
    
    // set alt info in gallery
	GetE("txtAlt").value = altText;
	
	// set copyright
	if (copyText !=	"")	{
		GetE("txtCopyright").value = copyText;
	}
	
	// check spacing values 
	if (isNaN(imgHSp) && imgHSp.indexOf("px") != -1)	{	
		imgHSp = imgHSp.substring(0, imgHSp.length - 2);
	}
	if (isNaN(imgVSp) && imgVSp.indexOf("px") != -1)	{	
		imgVSp = imgVSp.substring(0, imgVSp.length - 2);
	}

	if (imgHSp != "" || imgVSp != "") {
		imgBorder = true;
	}	
	
	if (imgBorder) {
		// set values for vspacing and hspacing(margin) and
		GetE("txtVSpace").value	= imgVSp;
		GetE("txtHSpace").value	= imgHSp;
		// enable input fields for spacing (margin)
        cms.galleries.checkGalleryCheckbox($('#' + cms.imagepreviewhandler.editorKeys['imgSpacing']), true);		
	}

	// set align attribute select box
    $('#' + cms.imagepreviewhandler.keys['editorFormatTabId'])
            .find('#' + cms.imagepreviewhandler.editorKeys['imgAlign'])
            .find('.cms-selectbox').selectBox('setValue',imgAlign);

	// set attributes in the advanced(extended) tab
    GetE("txtAttId").value = oImage.id;
    // language attribute
    var langDir = oImage.dir;
    if (langDir == "") {
        langDir = 'none';
    }	
    $('#' + cms.imagepreviewhandler.keys['editorAdvancedTabId'])
            .find('#' + cms.imagepreviewhandler.editorKeys['advLangDir'])
            .find('.cms-selectbox').selectBox('setValue', langDir);
    
	GetE("txtAttLangCode").value = oImage.lang;
	GetE("txtAttTitle").value = oImage.title;
	GetE("txtAttClasses").value = oImage.getAttribute("class", 2) || "";
	GetE("txtLongDesc").value = oImage.longDesc;
	GetE("txtAttStyle").value = cssTxt;

	if (oLink) {
		var lnkUrl = oLink.getAttribute("_fcksavedurl");
		if (lnkUrl == null) {
			lnkUrl = oLink.getAttribute("href", 2);
		}
		// set different link target, if image should be used as link
		// the link target is set in the advanced tab
		if (lnkUrl != sUrl) {
			GetE("txtLnkUrl").value = lnkUrl;            
            $('#' + cms.imagepreviewhandler.keys['editorAdvancedTabId']).find('#cmbLnkTarget').find('.cms-selectbox').selectBox('setValue',oLink.target);			
		}
		var idAttr = oLink.id;
		if (idAttr != null && idAttr.indexOf("limg_") == 0) {
            cms.galleries.checkGalleryCheckbox($('#' + cms.imagepreviewhandler.editorKeys['linkOriginal']), true);			
		}
	}
}

/**
 * Will be triggered in the advanced mode, when the 'reset alt text' button is clicked.<p>
 * 
 *  Resets the image alternative text to the original value (title property).
 */
function resetAltText() {
	var imgTitle = cms.galleries.activeItem.title;
	if (cms.galleries.activeItem.description != "") {
		imgTitle = cms.galleries.activeItem.description;
	}
	GetE("txtAlt").value = imgTitle;
}

/**
 * Will be triggered in the advanced mode, when the 'reset alt text' button is clicked.<p>
 * 
 * Resets the image copyright text to the original value (copyright property).
 */
function resetCopyrightText() {
	var copyText = cms.galleries.activeItem.copyright;
	if (copyText == null || copyText == "") {
		copyText = "";
	} else {
		copyText = "&copy; " + copyText;
	}
	GetE("txtCopyright").value = copyText;
}

/* Toggles the image spacing values. */
function setImageBorder() {
	if (insertImageBorder()) {
		var hSp = GetE("txtHSpace").value;
		if (hSp == "") {
			GetE("txtHSpace").value = "5";
		}
		var vSp = GetE("txtVSpace").value;
		if (vSp == "") {
			GetE("txtVSpace").value = "5";
		}
	} else {
		GetE("txtHSpace").value = "";
		GetE("txtVSpace").value = "";
	}
}

/**
 * Returns if the image spacing checkbox is checked or not. 
 */
function insertImageBorder() {
    var checked = $('#' + cms.imagepreviewhandler.editorKeys['imgSpacing']).hasClass('cms-checkbox-checked');    
	return checked;	
}

/**
 * Returns if the link to original image checkbox is checked or not.<p>
 * 
 * showEnhancedOptions = true
 */
function insertLinkToOriginal() {
    var checked = $('#' + cms.imagepreviewhandler.editorKeys['linkOriginal']).hasClass('cms-checkbox-checked');    
	return checked;	
}

/**
 * Returns if the sub title checkbox is checked or not.<p>
 * 
 * showEnhancedOptions = true
 */
function insertSubTitle() {
	var checked = $('#' + cms.imagepreviewhandler.editorKeys['fckInsertAlt']).hasClass('cms-checkbox-checked');    
	return checked;    
}

/**
 * Returns if the copyright checkbox is checked or not.<p>
 * 
 * showEnhancedOptions = true
 */
function insertCopyright() {
	var checked = $('#' + cms.imagepreviewhandler.editorKeys['fckInsertCr']).hasClass('cms-checkbox-checked');    
	return checked;    
}

/**
 *  Returns if enhanced options are used and sub title or copyright should be inserted. 
 */
// TODO: rename
function isEnhancedPreview() {
	return showEnhancedOptions && (insertSubTitle() || insertCopyright());
}

/**
 *  Returns if enhanced options should be shown.
 */
function isEnhanced() {
    return showEnhancedOptions;
}

/**
 *  The OK button was hit, called by editor button click event. 
 *  
 *  FCK API! Event callback
 *  JQuery!
 */
function Ok() {
    
    // TODO: is not used: !!! var resType = $('#results li[alt="' + $('#cms-preview').attr('alt') + '"]').data('type');       
    // if changed properties are not saved yet         
    if ($('button[name="previewSave"]').hasClass('cms-properties-changed')) {
        //text, title, yesLabel, noLabel, callback
        cms.util.dialogConfirmCancel('Do you want to save changed properties?', 'Save', 'Yes', 'No', 'Cancel', saveProperties);       
    } else { // not properties changed, call editor close function        
        closeEditor();  
        return true;
    }               
}

/* Saves changed properties and triggers ok button.*/
function saveProperties(isConfirmed) {
    if (isConfirmed) {
          var changedProperties = $('.cms-item-edit.cms-item-changed');      
          // build json object with changed properties
          var changes = {
             'properties': []
          };
          $.each(changedProperties, function() {
             var property = {};
             property['name'] = $(this).closest('div').attr('alt');
             property['value'] = $(this).val();
             changes['properties'].push(property);
          });
          
          var resType = $('#results li[alt="' + $('#cms-preview').attr('alt') + '"]').data('type');
          // save changes via ajax if there are any
          if (changes['properties'].length != 0) {
             $.ajax({
                'async': true,
                'url': cms.data.GALLERY_SERVER_URL,
                'data': {
                   'action': 'setproperties',
                   'data': JSON.stringify({
                      'path': $('#cms-preview').attr('alt'),
                      'properties': changes['properties']
                   })
                },
                'type': 'POST',
                'dataType': 'json',
                // TODO: error handling
                'success': triggerOkEvent
             });      
                
          } else {
              triggerOkEvent();
          }
      } else {
          triggerOkEvent();
      }        
   
}

/* Trigger the ok- button of the editor. */
function triggerOkEvent() {
    $('button[name="previewSave"]').removeClass('cms-properties-changed');
    window.parent.Ok();
}

/* Saves all image specific changes. */
function closeEditor() {
    var bHasImage = oImage != null;
	var imgCreated = false;

	if (!bHasImage) {
		oImage = FCK.InsertElement("img");
		// set flag that image is newly created
		imgCreated = true;
	}  else {
		oEditor.FCKUndo.SaveUndoStep();
	}

	updateImage(oImage);

	// now its getting difficult, be careful when modifying anything below this comment...

	if (isEnhancedPreview() && oLink) {
		// original link has to be removed if a span is created in enhanced options
		FCK.Selection.SelectNode(oLink);
		FCK.ExecuteNamedCommand("Unlink");
	}

	// now we set the image object either to the image or in case of enhanced options to a span element
	oImage = createEnhancedImage();

	if (showEnhancedOptions && oSpan != null && (oSpan.id.substring(0, 5) == "aimg_" || oSpan.id.substring(0, 5) == "timg_")) {
		// span is already present, select it
		FCK.Selection.SelectNode(oSpan);
		// remove child elements of span
		while (oSpan.firstChild != null) {
			oSpan.removeChild(oSpan.firstChild);
		}
	}

	if (!imgCreated) {
		// delete the selection (either the image or the complete span) if the image was not freshly created
		FCK.Selection.Delete();
		// now insert the new element
		oImage = oEditor.FCK.InsertElementAndGetIt(oImage);
	} else {
		// this handles the initial creation of an image, might be buggy...
		if (!oEditor.FCKBrowserInfo.IsIE) {
			// we have to differ here, otherwise the stupid IE creates the image twice!
			oImage = oEditor.FCK.InsertElementAndGetIt(oImage);
		} else if (isEnhancedPreview()) {
			// in IE... insert the new element to make sure the span is inserted
			oImage = oEditor.FCK.InsertElementAndGetIt(oImage);
		}
	}

	if (oImage.tagName != "SPAN") {
		// the object to insert is a simple image, check the link to set
		FCK.Selection.SelectNode(oImage);

		oLink = FCK.Selection.MoveToAncestorNode("A");

		var sLnkUrl = GetE("txtLnkUrl").value.Trim();
		var linkOri = "";

		if (insertLinkToOriginal()) {
			sLnkUrl = "#";
			linkOri = getLinkToOriginal();
		} else if (sLnkUrl == "#") {
			sLnkUrl = "";
		}

		if (sLnkUrl.length == 0) {
			if (oLink) {
				oLink.removeAttribute("class");
				FCK.ExecuteNamedCommand("Unlink");
			}
		} else {
			if (oLink) {  
				// remove an existing link and create it newly, because otherwise the "onclick" attribute does not vanish in Mozilla
				oLink.removeAttribute("class");
				FCK.ExecuteNamedCommand("Unlink");
				oLink = oEditor.FCK.CreateLink(sLnkUrl)[0];
			} else {
				// creating a new link
				if (!bHasImage) {
					oEditor.FCKSelection.SelectNode(oImage);
				}

				oLink = oEditor.FCK.CreateLink(sLnkUrl)[0];

				if (!bHasImage)	{
					oEditor.FCKSelection.SelectNode(oLink);
					oEditor.FCKSelection.Collapse(false);
				}
			}

			if (linkOri != "") {
				// set the necessary attributes for the link to original image
				try {
					if (useTbForLinkOriginal == true) {
						oLink.setAttribute("href", linkOri);
						oLink.setAttribute("title", GetE("txtAlt").value);
						oLink.setAttribute("class", "thickbox");
						sLnkUrl = linkOri;
					} else {
						oLink.setAttribute("onclick", linkOri);
					}
					oLink.setAttribute("id", "limg_" + cms.galleries.activeItem.hash);
					oImage.setAttribute("border", "0");
				} catch (e) {}
			}
			try {
				SetAttribute(oLink, "_fcksavedurl", sLnkUrl);
                var target = $('#' + cms.imagepreviewhandler.keys['editorAdvancedTabId'])
                    .find('#cmbLnkTarget').find('.cms-selectbox').selectBox('getValue');
				SetAttribute(oLink, "target", target);
			} catch (e) {}
		}
	} // end simple image tag
}

/* Creates the enhanced image HTML if configured. */
function createEnhancedImage() {
	if (isEnhancedPreview()) {
		// sub title and/or copyright information has to be inserted 
		var oNewElement = oEditor.FCK.EditorDocument.createElement("SPAN");
		// now set the span attributes      		
        var txtWidth = $('#' + cms.imagepreviewhandler.keys['formatTabId']).find('.cms-format-line[alt="width"]').find('input').val();
        var st = "width: " + txtWidth + "px;";		
        var al = $('#' + cms.imagepreviewhandler.keys['editorFormatTabId'])
            .find('#cmbAlign').find('.cms-selectbox').selectBox('getValue');
		if (al == "left" || al == "right") {
			st += " float: " + al + ";";
		}
		var imgVSp = GetE('txtVSpace').value;
		var imgHSp = GetE('txtHSpace').value;
		if (imgVSp != "" || imgHSp != "") {
			if (imgVSp == "") {
				imgVSp = "0";
			}
			if (imgHSp == "") {
				imgHSp = "0";
			}
			if (showEnhancedOptions && al != "") {
				var marginH = "right";
				if (al == "right") {
					marginH = "left";
				}
				st += "margin-bottom: " + imgVSp + "px; margin-" + marginH + ": " + imgHSp + "px;";
			} else {
				st += "margin: " + imgVSp + "px " + imgHSp + "px " + imgVSp + "px " + imgHSp + "px";
			}
		}
		oNewElement.style.cssText = st;
		SetAttribute(oNewElement, "id", "aimg_" + cms.galleries.activeItem.hash);

		// insert the image
		if (insertLinkToOriginal()) {
			var oLinkOrig = oEditor.FCK.EditorDocument.createElement("A");
			if (useTbForLinkOriginal == true) {
				oLinkOrig.href = getLinkToOriginal();
				oLinkOrig.setAttribute("title", cms.galleries.activeItem.title);
				oLinkOrig.setAttribute("class", "thickbox");
			} else {
				oLinkOrig.href = "#";
				oLinkOrig.setAttribute("onclick", getLinkToOriginal());
			}
			oLinkOrig.setAttribute("id", "limg_" + cms.galleries.activeItem.hash);
			oImage.setAttribute("border", "0");
			oLinkOrig.appendChild(oImage);
			oNewElement.appendChild(oLinkOrig);
		} else {
			// simply add image
			oNewElement.appendChild(oImage);
		}

		if (insertCopyright()) {
			// insert the 2nd span with the copyright information
			var copyText = GetE("txtCopyright").value;
			if (copyText == "") {
				copyText = "&copy; " + cms.galleries.activeItem.copyright;
			}
			var oSpan2 = oEditor.FCK.EditorDocument.createElement("SPAN");
			oSpan2.style.cssText = "display: block; clear: both;";
			oSpan2.className = "imgCopyright";
			oSpan2.id = "cimg_" + cms.galleries.activeItem.hash;
			oSpan2.innerHTML = copyText;
			oNewElement.appendChild(oSpan2);
		}

		if (insertSubTitle()) {
			// insert the 3rd span with the subtitle
			var altText = GetE("txtAlt").value;
			if (altText == "") {
				altText = cms.galleries.activeItem.title;
			}
			var oSpan3 = oEditor.FCK.EditorDocument.createElement("SPAN");
			oSpan3.style.cssText = "display: block; clear: both;";
			oSpan3.className = "imgSubtitle";
			oSpan3.id = "simg_" + cms.galleries.activeItem.hash;
			oSpan3.innerHTML = altText;
			oNewElement.appendChild(oSpan3);
		}

		// return the new object
		return oNewElement;
	} else {
		// return the original object
		return oImage;
	}
}


/* Creates the link to the original image. */
function getLinkToOriginal() {
	var linkUri = "";
	if (useTbForLinkOriginal == true) {
		linkUri += cms.galleries.activeItem.linkpath;
	} else {
		linkUri += "javascript:window.open('";
		linkUri += vfsPopupUri;
		linkUri += "?uri=";
		linkUri += cms.galleries.activeItem.linkpath;
		linkUri += "', 'original', 'width=";
		linkUri += cms.galleries.activeItem.width;
		linkUri += ",height=";
		linkUri += cms.galleries.activeItem.height;
		linkUri += ",location=no,menubar=no,status=no,toolbar=no');";
	}
	return linkUri;
}

/* Updates the image element with the values of the input fields. */
function updateImage(e) {
	var txtUrl = cms.galleries.activeItem.linkpath;
	var newWidth = cms.galleries.activeItem.width;
	var newHeight = cms.galleries.activeItem.height;
	if (cms.galleries.initValues.scale == null || cms.galleries.initValues.scale == "") {
		cms.galleries.initValues.scale = "c:transparent,t:4,r=0,q=70";
	} else {
		if (cms.galleries.initValues.scale.lastIndexOf(",") == cms.galleries.initValues.scale.length - 1) {
			cms.galleries.initValues.scale =cms.galleries.initValues.scale.substring(0, cms.galleries.initValues.scale.length - 1);
		}
	}

	if (cms.galleries.activeItem.isCropped) {
		var newScale = "";
		if (cms.galleries.initValues.scale != null && cms.galleries.initValues.scale != "") {
			newScale += ",";
		}
		newScale += "cx:" + cms.galleries.activeItem.cropx;
		newScale += ",cy:" + cms.galleries.activeItem.cropy;
		newScale += ",cw:" + cms.galleries.activeItem.cropw;
		newScale += ",ch:" + cms.galleries.activeItem.croph;
		cms.galleries.initValues.scale += newScale;
	} else if (cms.galleries.getContentHandler(cms.imagepreviewhandler.typeConst)['getScaleValue'](cms.galleries.initValues.scale, "cx") != "") {
		cms.galleries.initValues.scale = cms.galleries.getContentHandler(cms.imagepreviewhandler.typeConst)['removeScaleValue'](cms.galleries.initValues.scale, "cx");
		cms.galleries.initValues.scale = cms.galleries.getContentHandler(cms.imagepreviewhandler.typeConst)['removeScaleValue'](cms.galleries.initValues.scale, "cy");
		cms.galleries.initValues.scale = cms.galleries.getContentHandler(cms.imagepreviewhandler.typeConst)['removeScaleValue'](cms.galleries.initValues.scale, "cw");
		cms.galleries.initValues.scale = cms.galleries.getContentHandler(cms.imagepreviewhandler.typeConst)['removeScaleValue'](cms.galleries.initValues.scale, "ch");
	}

	cms.galleries.initValues.scale = cms.galleries.getContentHandler(cms.imagepreviewhandler.typeConst)['removeScaleValue'](cms.galleries.initValues.scale, "w");
	cms.galleries.initValues.scale = cms.galleries.getContentHandler(cms.imagepreviewhandler.typeConst)['removeScaleValue'](cms.galleries.initValues.scale, "h");
	var newScale = "";
	var sizeChanged = false;
	if (cms.galleries.initValues.scale != null && cms.galleries.initValues.scale != "") {
		newScale += ",";
	}
	if (cms.galleries.activeItem.newwidth > 0 && cms.galleries.activeItem.width != cms.galleries.activeItem.newwidth) {
		sizeChanged = true;
		newScale += "w:" + cms.galleries.activeItem.newwidth;
		newWidth = cms.galleries.activeItem.newwidth;
	}
	if (cms.galleries.activeItem.newheight > 0 && cms.galleries.activeItem.height != cms.galleries.activeItem.newheight ) {
		if (sizeChanged == true) {
			newScale += ",";
		}
		sizeChanged = true;
		newScale += "h:" + cms.galleries.activeItem.newheight;
		newHeight = cms.galleries.activeItem.newheight;
	}
	cms.galleries.initValues.scale += newScale;
	if (cms.galleries.activeItem.isCropped || sizeChanged) {
		txtUrl += "?__scale=" + cms.galleries.initValues.scale;
	}

	e.src = txtUrl;
	SetAttribute(e, "_fcksavedurl", txtUrl);
	SetAttribute(e, "alt"   , GetE("txtAlt").value);
	SetAttribute(e, "width" , newWidth);
	SetAttribute(e, "height", newHeight);
	SetAttribute(e, "border", "");

    var align = $('#' + cms.imagepreviewhandler.keys['editorFormatTabId'])
        .find('#' + cms.imagepreviewhandler.editorKeys['imgAlign']).find('.cms-selectbox').selectBox('getValue');	
    SetAttribute(e, "align" , align);

	var styleAttr = "";
	SetAttribute(e, "vspace", "");
	SetAttribute(e, "hspace", "");
	if (!isEnhancedPreview()) {    		
            var imgAlign = align;
    		var vSp = GetE("txtVSpace").value;
    		var hSp = GetE("txtHSpace").value;
    		if (vSp == "") {
			vSp = "0";
		}
		if (hSp == "") {
			hSp = "0";
		}
    		if (showEnhancedOptions && imgAlign == "left") {
    			styleAttr = "margin-bottom: " + vSp + "px; margin-right: " + hSp + "px;";
    		} else if (showEnhancedOptions && imgAlign == "right") {
    			styleAttr = "margin-bottom: " + vSp + "px; margin-left: " + hSp + "px;";
    		} else {
			SetAttribute(e, "vspace", GetE("txtVSpace").value);
			SetAttribute(e, "hspace", GetE("txtHSpace").value);
		}
		if (insertLinkToOriginal()) {
			SetAttribute(e, "border", "0");
		}
	}

	// advanced attributes

	var idVal = GetE("txtAttId").value;
	if (idVal == "" || idVal.substring(0, 5) == "iimg_") {
		idVal = "iimg_" + cms.galleries.activeItem.hash;
	}
	SetAttribute(e, "id", idVal);

    var langDir = $('#' + cms.imagepreviewhandler.keys['editorAdvancedTabId'])
        .find('#' + cms.imagepreviewhandler.editorKeys['advLangDir']).find('.cms-selectbox').selectBox('getValue');
	SetAttribute(e, "dir", langDir);
	SetAttribute(e, "lang", GetE("txtAttLangCode").value);
	SetAttribute(e, "title", GetE("txtAttTitle").value);
	SetAttribute(e, "class", GetE("txtAttClasses").value);
	SetAttribute(e, "longDesc", GetE("txtLongDesc").value);

	styleAttr += GetE("txtAttStyle").value;
	if (oEditor.FCKBrowserInfo.IsIE) {
		e.style.cssText = styleAttr;
	} else {
		SetAttribute(e, "style", styleAttr);
	}
}