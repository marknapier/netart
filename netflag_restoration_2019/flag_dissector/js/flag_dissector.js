window.FlagDissector = (function () {
  // Colors (originally from FlagDescription.java)
  var colors = [
    "255,255,255", // white
    "0,0,0", // black
    // blue
    "0,35,119", //  "usa australia, uk dark blue"
    "0,49,156", //  "(armenia, cambodia) finland, columbia, thailand, south africa, namibia, Bahamas Barbados medium blue
    "0,0,204", // medium bright blue
    "0,107,198", // "light blue  uzbekistan, guatemala, elsalvador, honduras, nicaragua, sweden, greece, estonia"
    "8,123,206", // azerbaijan, israel light dull blue
    "102,204,255", // "ukraine, luxembourg, armenia, botswana, san marino light blue"
    // green
    "0,91,24", // dark green
    "0,102,0", // medium green, saudi arabia
    "0,104,71", //  mexico dark dull green
    "56,126,41", // uzbekistan
    "53,150,43", // "togo, jordan, "     0,128,0=brazil
    "0,153,0", // medium green
    "0,191,0", // rwanda green
    // yellow - orange
    "255,255,0", // "ukraine, lots of countries, brightyellow"
    //"255,214,0",   // NOW IS 255,204,0.  "benin, south africa, barbados"
    "255,204,0", // "germany, cyprus, vatican, sweden, lots others, yellow gold"
    "255,153,0", // "cote divoire, armenia, india orange"
    "255,90,0", //  ireland
    // red - redorange
    "222,57,8", //  uganda rust red-orange
    "255,0,0", // "austria, lots of countries, bright red"
    "245,30,48", // "puerto rico, angola, canada, bright red"
    "241,0,67", //  united states red
    "222,33,16", // "thailand, lots of countries, medium red"
    //"204,51,51",   // NOW IS 204,0,0   azerbiajan
    "204,0,0", // bolivia gambia cambodia iraq, UK, mexico
    //"188,2,4",   // NOW IS 222,33,16   denmark orange red
    "153,0,0" //  latvia georgia dark dull red
  ];

  // Keywords in alpha order (originally from FlagDescription.java)
  var keywords = [
    "africa",
    "agriculture",
    "blood",
    "catholic",
    "communism",
    "compassion",
    "courage",
    "energy",
    "equality",
    "freedom",
    "forest",
    "grain",
    "harmony",
    "heaven",
    "hope",
    "innocence",
    "islam",
    "liberty",
    "loyalty",
    "peace",
    "progress",
    "protestant",
    "purity",
    "revolution",
    "royalty",
    "sacrifice",
    "suffering",
    "sand",
    "sea",
    "sky",
    "snow",
    "sun",
    "strength",
    "unity",
    "valour",
    "virtue",
    "wealth"
  ];


  // marks the flag element that the user is editing on the left side flag
  var marker = {
    type: 'rect',
    x: 0,
    y: 0,
    w: 0,
    h: 0,
    visible: false,
    dragging: false,
  };
  // contains all flag elements by country
  var flagElements = {};
  // the left-side flag where the user can create/change flag elements
  var editContext = document.getElementById('flag-image').getContext('2d');
  // the right-side flag that shows the current state of the flag elements
  var displayContext = document.getElementById('flag-components').getContext('2d');

  // Actual pixel dimensions of the net.flag.
  const FLAG_WIDTH = 700;
  const FLAG_HEIGHT = 440;

  // Net.flag stores coordinates as percentages of the flag width and height.
  // This allows flags to scale to any size, and potentiall change proportions.

  function xPercent(x) {
    return (x / FLAG_WIDTH).toFixed(3) * 1000;
  }

  function yPercent(y) {
    return (y / FLAG_HEIGHT).toFixed(3) * 1000;
  }

  function percentToX(xp) {
    return xp * 0.001 * FLAG_WIDTH;
  }

  function percentToY(yp) {
    return yp * 0.001 * FLAG_HEIGHT;
  }

  function getFlagPos(e) {
    var relX = Math.round(e.pageX - $('#flag-image').offset().left);
    var relY = Math.round(e.pageY - $('#flag-image').offset().top);
    return {x: relX, y: relY};
  }

  // Marker ////////////////////////////////////////////////////////////////////

  function clearFlag(context) {
    context.clearRect(0, 0, context.canvas.width, context.canvas.height);
    // const canvas = document.getElementById('flag-image');
    // const ctx = canvas.getContext('2d');
    // ctx.clearRect(0, 0, canvas.width, canvas.height);
  }

  function addPolyPoint(pos) {
    var pointStr = $('#poly-points').val();
    if (pointStr.length > 1) {
      pointStr += ',\n';
    }
    $('#poly-points').val( `${pointStr}${xPercent(pos.x)},${yPercent(pos.y)}` );
  }

  function startElement(e) {
    var pos = getFlagPos(e);
    $('#marker').show();
    marker.visible = true;
    marker.dragging = true;
    marker.x = pos.x;
    marker.y = pos.y;
  }

  function finishElement(e) {
    marker.dragging = false;
    if (marker.type === 'poly') {
      var pos = getFlagPos(e);
      // if mouse didn't move between down and up, it's a click. Add a point to polygon.
      if (pos.x === marker.x && pos.y === marker.y) {
        addPolyPoint(pos);
        updateMarkerFromForm();
      }
    }
  }

  function updateMarkerFromMouseEvent(e) {
    if (marker.visible && marker.dragging) {
      var pos = getFlagPos(e);
      marker.w = pos.x - marker.x;
      marker.h = pos.y - marker.y;

      // for circle and star constrain proportions
      marker.type = selectedElementType();
      if (shortTypeCode(marker.type) === 'circ' || shortTypeCode(marker.type) === 'star') {
        marker.h = marker.w;
      }

      // store percents in the form (not absolute pixel positions)
      $('#markerX').val(xPercent(marker.x));
      $('#markerY').val(yPercent(marker.y));
      $('#markerW').val(xPercent(marker.w));
      $('#markerH').val(yPercent(marker.h));

      drawMarker();
    }
  }

  function updateMarkerFromForm() {
    var flagElement = getElementValsFromForm();
    marker.type = flagElement.type;

    // // convert percents to absolute pixel values
    marker.x = percentToX(flagElement.x);
    marker.y = percentToY(flagElement.y);
    marker.w = percentToX(flagElement.w);
    marker.h = percentToY(flagElement.h);

    // only star type uses these
    marker.starNumpoints = flagElement.starNumpoints;
    marker.starSpikyness = flagElement.starSpikyness;
    marker.starAngle = flagElement.Angle;

    // only polygons use these
    marker.polyPointStr = flagElement.polyPointStr;  // netflag style data
    marker.polyPoints = FlagShapes.convertDataPointsToPixels(flagElement.polyPointStr, FLAG_WIDTH, FLAG_HEIGHT);

    drawMarker();
  }

  function getElementValsFromForm() {
    var flagElement = {
      abbrev: $('#abbrev').val(),
      name: $('#element-name').val(),
      type: $('#element-type').val(),
      x: $('#markerX').val(),
      y: $('#markerY').val(),
      w: $('#markerW').val(),
      h: $('#markerH').val(),
      color: $('#color').css('background-color') || $('#color').val() || '#def',
      meaning: $('#meaning').val(),
      keywords: $('#keywords').val(),
    };

    if (shortTypeCode(flagElement.type) === 'star') {
      flagElement.starNumpoints = $('#star-numpoints').val();
      flagElement.starSpikyness = $('#star-spikyness').val();
      flagElement.starAngle = $('#star-angle').val();
    }

    if (shortTypeCode(flagElement.type) === 'poly') {
      flagElement.polyPointStr = $('#poly-points').val().replace(/\n/g, '');
    }

    return flagElement;
  }

  // return the three extra flag parameters that vary by type. These will be used in the netflag data file,
  // so they are formatted to match that files standards. 
  function getElementExtraParams(flagEl) {
    if (flagEl.type === 'poly') {
      return {
        one: `\"${flagEl.polyPointStr}\"`,   //"
        two: 0,
        three: 0,
      };
    }
    else if (flagEl.type === 'star') {
      return {
        one: flagEl.starAngle, 
        two: flagEl.starNumpoints,
        three: flagEl.starSpikyness,
      };
    }
    else {
      return {
        one: 0,
        two: 0,
        three: 0,
      };
    }
  }

  function addMarkedToFlag() {
    var country = $('#country').val();
    var newElement = getElementValsFromForm();

    if (!newElement.name) {
      alert('Enter an element name');
    }
    else if (!country) {
      alert('Enter a country name at top of the page');
    }
    else if (!newElement.abbrev) {
      alert('Enter a country abbreviation at top of the page');
    }
    else {
      removeElementRow(newElement.name);
      flagElements[country] = flagElements[country] || [];
      flagElements[country].push(newElement);
      drawElementList();
      drawFlag();
    }
  }

  function addNewLines(polyPointStr) {
    if (polyPointStr) {
      var datapoints = polyPointStr.split(',');
      var stringWithLineBreaks = '';
      for (let i=0; i < datapoints.length; i += 2) {
        stringWithLineBreaks += `${datapoints[i]},${datapoints[i+1]},\n`;
      }
      stringWithLineBreaks = stringWithLineBreaks.slice(0, -2);
      return stringWithLineBreaks;      
    }
    return '';
  }

  function setMarkedElement(flagEl) {
    $('#abbrev').val(flagEl.abbrev);
    $('#element-name').val(flagEl.name);
    $('#element-type').val(flagEl.type);
    $('#markerX').val(flagEl.x);
    $('#markerY').val(flagEl.y);
    $('#markerW').val(flagEl.w);
    $('#markerH').val(flagEl.h);

    $('#star-numpoints').val(flagEl.starNumpoints);
    $('#star-spikyness').val(flagEl.starSpikyness);
    $('#star-angle').val(flagEl.starAngle);

    $('#poly-points').val(addNewLines(flagEl.polyPointStr));

    $('#color').val(flagEl.color).css({backgroundColor: flagEl.color});
    $('#meaning').val(flagEl.meaning);
    $('#keywords').val(flagEl.keywords);
    pickElementType();
    updateMarkerFromForm();
  }

  function shortTypeCode(typecode) {
    return typecode.substring(0,4);
  }

  function selectedElementType() {
    return $('#element-type').val();
  }

  function makeImageFilePath() {
    var country = $('#country').val();
    return `images/${country}_image.gif`;
  }

  function convertStringToPoints(datapointsStr) {
    var datapoints = datapointsStr.split(',');
    var pixelPoints = [];
    for (let i=0; i < datapoints.length; i += 2) {
      pixelPoints.push({
        x: datapoints[i],
        y: datapoints[i+1],
      });
    }
    return pixelPoints;
  }

  // Draw one flag element to the given flag context.
  function drawFlagElement(context, element) {
    var type = element.type.substring(0, 4);

    if (type === 'circ') {
      FlagShapes.drawCircle(context, element.x, element.y, element.w, element.color);
    } else if (type === 'tria') {
      FlagShapes.drawTriangleRight(context, element.x, element.y, element.w, element.h, element.color);
    } else if (type === 'star') {
      FlagShapes.drawStar(context, element.x, element.y, element.w, element.starNumpoints, element.starSpikyness, element.starAngle, element.color);
    } else if (type === 'poly') {
      FlagShapes.drawPolygon(context, element.polyPoints, element.color);
    } else if (type === 'imag') {
      loadImage(makeImageFilePath(), (img) => {
        FlagShapes.drawImage(context, img, element.x, element.y, element.w, element.h);
      });
    } else {
      FlagShapes.drawRect(context, element.x, element.y, element.w, element.h, element.color);
    }
  }

  function loadImage (src, callback, errorCallback ) {
      var img = new Image();
      img.onload = function() {
          callback(this);
      };
      img.onerror = function () {
          errorCallback && errorCallback(this);
      };
      img.src = src;
  }

  function drawMarker() {
    marker.visible = true;
    marker.color = '#0000ff88';
    clearFlag(editContext);
    drawFlagElement(editContext, marker);
    // drawFlagElement(editContext, marker);  //convertPercentToPixels(marker)
  }

  function trackMouseCoords(e) {
    var pos = getFlagPos(e);
    $('#mouseinfo').empty().append(`xy: ${pos.x},${pos.y} xy%: ${xPercent(pos.x)},${yPercent(pos.y)}`);
    updateMarkerFromMouseEvent(e);
  }

  // Flag Elements ////////////////////////////////////////////////////////////////////

  // CSS color string is like "rgb(120, 30, 255)"
  // netflag data file stores color as three ints "120, 30, 255"
  function stripRGB(rgbColorString) {
    return rgbColorString.replace('rgb(', '').replace(')', '');
  }

  function drawElementRowText(el) {
    var extraParams = getElementExtraParams(el);
    var extraParamsStr = `${extraParams.one}\t${extraParams.two}\t${extraParams.three}`;
    var keywords = el.keywords || '';
    return `${el.abbrev}\t${el.name}\t\"${stripRGB(el.color)}\"\t${el.type}\t${el.x}\t${el.y}\t${el.w}\t${el.h}\t${extraParamsStr}\t\"${keywords}\"\t\"${el.meaning}\"`;   // "
  }

  function makeElementButtons(elName) {
    var $bttnRemove = $(`<button class="el_row_bttn" id="btn_remove_${elName}">X</button>`);
    $bttnRemove.on('click', function () {
      removeElementRow(elName);
    });
    var $bttnUp = $(`<button class="el_row_bttn" id="btn_remove_${elName}">Up</button>`);
    $bttnUp.on('click', function () {
      moveElementUp(elName);
    });
    var $bttnEdit = $(`<button class="el_row_bttn" id="btn_edit_${elName}">&lt;&lt; Edit</button>`);
    $bttnEdit.on('click', function () {
      editElementRow(elName);
    });
    return [$bttnEdit, $bttnUp, $bttnRemove];
  }

  function moveElementUp(elName) {
    var country = $('#country').val();
    var flagElement = flagElements[country] && flagElements[country].find(el => el.name === elName);
    if (flagElement) {
      var index = flagElements[country].indexOf(flagElement);
      if (index > 0) {
        var tmp = flagElements[country][index - 1];
        flagElements[country][index - 1] = flagElement;
        flagElements[country][index] = tmp;
        drawElementList();
        drawFlag();
      }
    }
  }

  function removeElementRow(elName) {
    var country = $('#country').val();
    var flagElement = flagElements[country] && flagElements[country].find(el => el.name === elName);
    if (flagElement) {
      var index = flagElements[country].indexOf(flagElement);
      flagElements[country].splice(index, 1);
      drawElementList();
    }
  }

  function editElementRow(elName) {
    var country = $('#country').val();
    var flagElement = flagElements[country].find(el => el.name === elName);
    if (flagElement) {
      setMarkedElement(flagElement);
    }
  }

  function drawElementRow(el) {
    var $div = $('<div class="element-row"></div>');
    $div.append(makeElementButtons(el.name));
    $div.append(`<span>${drawElementRowText(el)}</span>`);
    return $div;
  }

  function drawElementList() {
    var country = $('#country').val();
    $('#flag-element-list').empty();

    if (flagElements[country]) {
      flagElements[country].forEach((element) => {
        $('#flag-element-list').append(drawElementRow(element));
      });
    }
  }

  // Netflag data files store coordinates as percentages of flag width and height. Percents have 1
  // decimal of precision, e.g. 35.4%, and are stored as ints like 354.
  // This function converts the netflag percent to absolute pixels.
  function convertPercentToPixels(elementWithPercents) {
    var elementWithPixels = Object.assign({}, elementWithPercents);
    elementWithPixels.x = percentToX(elementWithPercents.x);
    elementWithPixels.y = percentToY(elementWithPercents.y);
    elementWithPixels.w = percentToX(elementWithPercents.w);
    elementWithPixels.h = percentToY(elementWithPercents.h);
    if (elementWithPercents.type === 'poly') {
      elementWithPixels.polyPoints = FlagShapes.convertDataPointsToPixels(elementWithPercents.polyPointStr, FLAG_WIDTH, FLAG_HEIGHT);
    }
    return elementWithPixels;
  }

  function drawFlag() {
    var country = $('#country').val();
    clearFlag(displayContext);

    if (flagElements[country]) {
      flagElements[country].forEach((elementVals) => {
        drawFlagElement(displayContext, convertPercentToPixels(elementVals));
      });
    }
  }

  function drawStoredFlagElements() {
    var shapedata = '';
    Object.keys(flagElements).forEach((country) => {
      flagElements[country].forEach((element) => {
        shapedata += drawElementRowText(element) + '\n';
      });
    });

    $('#shape-data').val(shapedata);
  }

  function toggleBGSizeStyle() {
    var bgSizeStyle = $("input[name='bgsize']:checked").val();
    $('#flag-image').css('background-size', bgSizeStyle);
  }

  function copyTextToClipboard(text) {
    var textArea = document.getElementById("import-export-json");
    textArea.value = text;
    textArea.focus();
    textArea.select();

    try {
      var successful = document.execCommand('copy');
      if (successful) {
        alert("Copied flag data to clipboard");
      }
    } catch (err) {
      alert('Oops, unable to copy', err);
    }

    // document.body.removeChild(textArea);
  }

  function copyStorageToClipboard() {
    var data = JSON.parse(window.localStorage.getItem('allFlagElements'));
    var text = JSON.stringify(data, null, 4);
    copyTextToClipboard(text);
  }

  function importJSON() {
    var data = JSON.parse($('#import-export-json').val());
    flagElements = data;
    refreshUI();
  }

  ////////////////////////////////////////////////////////////////////////////////////

  function saveToStorage() {
    window.localStorage.setItem('allFlagElements', JSON.stringify(flagElements));
    drawStoredFlagElements();
  }

  function retrieveFromStorage() {
    flagElements = JSON.parse(window.localStorage.getItem('allFlagElements'));
    flagElements = flagElements || {};
  }

  function getFlagElements(countryName) {
    return countryName && flagElements[countryName];
  }

  function setCountry(country) {
    var flag = getFlagElements(country);
    if (flag) {
      $('#abbrev').val(flag[0].abbrev);
    }
    $('#country').val(country);
    $('#topbar img').attr('src', `flags/${country.toLowerCase()}.png`);
    $('#flag-image').css('background-image', `url(flags/${country.toLowerCase()}.png)`);
    drawFlag();
    drawElementList();
  }

  function setCountryFromDropdown() {
    setCountry($('#country').val());
  }

  function pickElementType() {
    var type = selectedElementType();
    var typecode = shortTypeCode(type);
    $('#element-type-options').hide();
    $('#element-type-options div').hide();
    if (typecode === 'star' || typecode === 'poly') {
      $('#element-type-options').show();
      $(`#${typecode}-options`).show();
      if (typecode === 'poly') {
        // bounds of polygon are always the full flag - polyPoints determines shape of poly
        $('#markerX').val(0);
        $('#markerY').val(0);
        $('#markerW').val(1000);
        $('#markerH').val(1000);
      }
    }
    updateMarkerFromForm();
  }

  function addKeyword() {
    var keyword = $('#keyword-select').val();
    var keywords = $('#keywords').val();

    if (keywords.indexOf(keyword) === -1) {
      keywords = keywords.length > 1 ? keywords + ',' : keywords;

      $('#keywords').val( keywords + keyword );
    }
  }

  function drawStoredFlagCountryNames() {
    var savedFlagNames = Object.keys(flagElements).sort();
    var html = `There are ${savedFlagNames.length} saved flags: `;

    function makeAHrefTag(countryName) {
      var a = document.createElement('a');
      a.appendChild(document.createTextNode(countryName));
      a.title = `Edit flag for ${countryName}`;
      a.href = "#";
      a.className = 'flag-link';
      a.addEventListener('click', (function (cname) {
              return (e) => {
                e.preventDefault();
                setCountry(cname);
              }
            }(countryName))
      );
      return a;
    }

    $('#existing-flags').empty().append(html);

    savedFlagNames.forEach((countryName) => {
      $('#existing-flags').append( makeAHrefTag(countryName) ).append(' ');
    });
  }

  function makeSelectElement(id = 'my-select', values = []) {
    var select = document.createElement('select');
    var option;

    select.setAttribute('id', id);

    for (var i=0; i < values.length; i += 1) {
        option = document.createElement('option');
        option.setAttribute('value', values[i]);
        option.appendChild(document.createTextNode(values[i]));
        select.appendChild(option);
    }

    return select;
  }

  function setupEvents() {
    $('#country').change(setCountryFromDropdown);

    $('#flag-image')
      .on('mousemove', trackMouseCoords)
      .on('mousedown', startElement)
      .on('mouseup', finishElement);

    $('#addToFlag')
      .on('click', addMarkedToFlag);
    $('#save-flag')
      .on('click', saveToStorage);

    $("input[name='bgsize']").change(toggleBGSizeStyle);
    $('#element-type').change(pickElementType);
    $('#markerX').on('input', updateMarkerFromForm);
    $('#markerY').on('input', updateMarkerFromForm);
    $('#markerW').on('input', updateMarkerFromForm);
    $('#markerH').on('input', updateMarkerFromForm);

    $('#star-numpoints').on('input', updateMarkerFromForm);
    $('#star-spikyness').on('input', updateMarkerFromForm);
    $('#star-angle').on('input', updateMarkerFromForm);
    $('#poly-points').on('input', updateMarkerFromForm);

    $('#copy-to-clipboard')
      .on('click', copyStorageToClipboard);
    $('#import')
      .on('click', importJSON);

    var keywordSelect = makeSelectElement('keyword-select', keywords);
    $(keywordSelect).change(addKeyword);
    $('#keyword-list').append(keywordSelect);
  }

  function refreshUI() {
    drawStoredFlagCountryNames();
    drawStoredFlagElements();
  }

  function init() {
    setupEvents();
    retrieveFromStorage();
    refreshUI();
  }

  return {
    init,
  };

}());
