<?php

require_once("html_guts.php");

$SHREDDER_TITLE = "Shredder 1.1";
$SHREDDER_URL_ROOT = "/shredder/php/shred_v1.php";
$SHREDDER_URL = $SHREDDER_URL_ROOT . "?url=";
$SHREDDER_WIDTH_PIXELS = "800";   // Shredder 1.0 (1998) was designed to fit an 800x600 pixel window (see setShredderScale())

$URLstring = sanitizeUrl(getUserURL());

startHTMLpage();
startFrame($URLstring);
shredAll($URLstring);
endFrame();
endHTMLpage();

// http://jodi.org/cgi-bin/as.cgi
// http://asdfg.jodi.org/cgi-bin/zxcvb.cgi

// here's an image that works
// http://asdfg.jodi.org/-------------------------------/-------------------------------/-------------------------------/-------------------------------/-----------------------941ht358/gif/bl.gif

// http://wwwwwww.jodi.org/00010/index.html
// http://wwwwwwwww.jodi.org/
// http://404.jodi.org
// http://posthtml.org/
// plexus.org
// http://superbad.com/1/beanie/index.html
// http://maxpaynecheatsonly.jodi.org/mp1a34.html

//--------------------------------------------------------------------------------------------------

// ------------------------------------------------
// Shred the given url and any further nested urls
// ------------------------------------------------
function shredAll($URLstring) {
	// limit how many urls we'll shred
	$maxUrls = 7;
	$urlCount = 0;

	// Exit if script was not called by the Shredder interface (probably somebody hacking)
	$refurl = isset($_SERVER['HTTP_REFERER']) ? $_SERVER['HTTP_REFERER'] : '';
	if ( !(isset($refurl) && strpos($refurl, '/shredder/toolbar.html') !== false) ) {
		errorExit('&nbsp;:(');
	}

	// push URL onto stack
	$URLstack = array($URLstring);

	// seed the random number generator
	$randomSeed = getParam('srand');  
	if (!isset($randomSeed)) {
		$randomSeed = time();
	}
	mt_srand($randomSeed);

	// start the shredded output div and set the scale to imitate an 800 pixel width screen
	print "<div id='shredder-out'>\n";
	print "<script>	setShredderScale(); </script>\n";

	// shred() may push more URLs onto stack (if shredding a frameset) so loop until all are shredded
	while (($url = array_shift($URLstack)) && $urlCount++ < $maxUrls) {
		shred($url, $URLstack);
	}

	// show a link to this shredded page, with the random seed (so page will display exactly the same next time it's viewed)
	print makePermalink($URLstring, $randomSeed);

	print "</div>\n";
}

// ------------------------------------------------
// Shred a single url
// ------------------------------------------------
function shred($url, &$URLstack) {
	// clean and parse the URL
	$urlParts = testURL(sanitizeURL($url));
	if ($urlParts === FALSE || empty($urlParts["host"])) {
		errorExit('Not a valid URL');
	}
	else {
		// Show the starting URL
		print "<!-------- Start Shred: $url -------->\n";

		// get the html headers
		$shredURL = buildURL($urlParts);
		$headers = getHeaders($shredURL);

		// get the actual page url (might change if $url redirected to another page)
		$shredURL = makeFullHref($headers['location'], $urlParts);

		// get the html page 
		$html = getHTML($shredURL);
		// guts('HTMLHTMLHTMLHTML 1', $html);

		// parse images, links, etc. from the html
		$elements = getHTMLElements($html, $shredURL);
		$elements['headers'] = $headers['headerText'];

		// write out the shredded page
		outputShreddedHTML($elements, $shredURL);

		// add frameset urls onto stack (this will shred pages nested in frames)
		foreach($elements['frames'] as $frameURL) {
			array_push($URLstack, $frameURL);
		}
	}
}

// Output the 'shredded' HTML file
function outputShreddedHTML($elements, $url) {
	$urlParts = testURL($url);

	// guts('HTMLHTMLHTMLHTML2', $elements['bgcolors']);
	// guts('HTMLHTMLHTMLHTML2 [0]', $elements['bgcolors'][0]);
	// guts('HTMLHTMLHTMLHTML2 bodyBGC', $elements['bodyBGColor']);

	$html = $elements['headers'] . $elements['html'];
	$colors = $elements['colors'];
	$images = $elements['images'];
	$videos = $elements['videos'];
	$mp4s = $elements['mp4s'];
	$links = $elements['links'];
	$bodyBGC = (isset($elements['bodyBGColor']) && $elements['bodyBGColor'] != "") ? $elements['bodyBGColor'] : 
				((isset($elements['bgcolors']) 
									&& 
									sizeof($elements['bgcolors']) > 0) ? 
								$elements['bgcolors'][0] : 
								'#fff');

	// Show the URL
	print "<!-------- Output Shred: $url -------->\n";

	// Set body BG color
	print "<SCRIPT>document.body.style.backgroundColor = '" . $bodyBGC . "';</SCRIPT>\n";
	
	// Set page title and location
	print "<SCRIPT>setShredderTitle('" . $elements['title'] . "'); setShredderLocation('" . $url . "');</SCRIPT>\n";

	// Pick an image and tile in a div
	print "<!-------- Background -------->\n";
	$ran3 = randint(0, 2);
	$ran100 = randint(0, 100);
	$bgL = (100 + ($ran3 * $ran100)) . "px";
	$bgw = (400 + ($ran100 * 2)) . "px";
	$bgh = (400 + $ran100) . "px";
	if ( sizeof($images) > 0 ) {
		$image = $images[randint(0, sizeof($images)-1)];
		print "<div id='BGLyr' style='position:absolute; left:$bgL; top:20px; width:$bgw; height:$bgh; background-image:url(" . makeFullHref($image, $urlParts) . "); clip:rect(20px $bgw $bgh 30px); z-index:0'>\n" ;
		print "</div>\n\n";
	}

	// write out the HTML text in 12 layers
	$htmlbytesize = strlen($html);
	print "<!-------- Text ($htmlbytesize) -------->\n";
	$ranTxtLyr = randint(1, 10);
	$chunkLen = strlen($html) / 12;
	for ( $i=0; $i < 12; $i++ ) {
		$chunkOfHTML = substr( $html, $i * $chunkLen, $chunkLen );
		$topPos = ($i * 20) . "px";
		$fontClass = "smalltext";
		$fontColor = "#c0c0c0";
		$inputTag = "";
		if (sizeof($colors) > 1) {
			$rIdx = randint(0, sizeof($colors)-1);
			$fontColor = $colors[$rIdx];
		}
		if ($fontColor == $bodyBGC) {	# insure font color != bgcolor
			$fontColor = "#20f040";
		}
	// 	if (sizeof($formInputs) > 0) {
	// 		$inputTag = array_shift($formInputs);
	// 		$inputTag = "<form>" . $inputTag . "<\/form>\n";
	// 	}
		if ($i == $ranTxtLyr) {
			// one random layer will be large text
			$fontClass = "verylargetext";
		} 
		else if (preg_match('/[;\/]PRE\&/i', $chunkOfHTML) || preg_match('/COURIER/i', $chunkOfHTML)) {
			// if the layer has a pre tag in it then show the text as Courier font 
			// (the regex above looks odd because <pre> has been converted to &lt;pre&gt;) 
			$fontClass = "monospacetext";
		}
		print "<div id=\"Lyr$i\" style=\"position:absolute; left:$topPos; top:$topPos; width:60px; height:800px; z-index:$i\">\n" ;
		print "<p class='$fontClass' style='color:$fontColor'> $chunkOfHTML </p>\n";
		print "<p> $inputTag </p>\n";
		print "</div>\n\n";
	}

	print "<!-------- Fg Img -------->\n";
	if ( sizeof($images) > 0 ) {
		$image = $images[randint(0, sizeof($images)-1)];
		$fgL = (450 + $ran100) . "px";
		$fgT = (150 + $ran100) . "px";
		print "<div id=\"FlkLyr$i\" style=\"position:absolute; left:$fgL; top:$fgT; width:220px; height:420px; z-index:$i\">\n" ;
		print "<img src='" . makeFullHref($image, $urlParts) . "' style='width:100%; height:100%'>\n";
		print "</div>\n\n";
	}

	print "<!-------- mp4s -------->\n";
	while ($mp4url = array_shift($mp4s)) {
		$rX = "" . randint(150, 500) . "px";
		$rY = "" . randint(10, 50) . "px";
		$clipW = "" . randint(50, 350) . "px";
		$clipL = "" . randint(0, $clipW-20) . "px";
		print "<div id='VidLyr$i' style='position:absolute; left:$rX; top:$rY; width:700px; height:700px; overflow:hidden; clip:rect(0px $clipW 700px $clipL); z-index:$i;'>\n";
		print "  <video id='video1' controls='' autoplay loop style='height:100%'>\n";
		print "    <source src='$mp4url' type='video/mp4'>\n";
		print "  </video>\n";
		print "</div>\n\n";
		$i++;
	}

	print "<!-------- Images -------->\n";
	while ($image = array_shift($images)) {
		$imageURL = makeFullHref($image, $urlParts);
		$rX = "" . randint(0, 500) . "px";
		$rY = "" . randint(0, 300) . "px";
		$rW = "" . randint(20, 50) . "px";
		// split about 50/50
		if ( randint(0, 100) > 50 ) {
			// tile the image in the background of a shortish div
			print "<div id=\"ImgLyr$i\" style=\"position:absolute; left:$rX; top:$rY; width:$rW; height:200px; background-image:url($imageURL); z-index:$i\">\n" ;
			print "</div>\n\n";
		} else {
			// stretch the image to fit a longer div
			print "<div id=\"ImgLyr$i\" style=\"position:absolute; left:$rX; top:$rY; width:$rW; height:420px; z-index:$i\">\n" ;
			print "<img src='$imageURL' style='width:100%; height:100%'>\n";
			print "</div>\n\n";
		}
		$i++;
	}

	print "<!-------- Links -------->\n";
	$i = 100;
	while ($link = array_shift($links)) {
		$rpos = randint(50, 450) . "px";
		$link = makeFullHref($link, $urlParts);
		print "<div id=\"LnkLyr$i\" style=\"position:absolute; left:10px; top:$rpos; width:400px; height:10px; z-index:$i\">\n" ;
		print "<A HREF='$link' onClick='shred(\"$link\");return false;'>$link</A><P>\n";
		print "</div>\n\n";
		$i++;
	}

	print "<!-------- colors -------->\n";
	$i = 400;
	while ($color = array_shift($colors)) {
		$topPos = (($i - 400) * 10) . "px";
		print "<div id=\"ClrLyr$i\" style=\"position:absolute; left:40px; top:$topPos; height:20px; background-color:$color; z-index:$i\">\n" ;
		print "Color=$color<P>\n";
		print "</div>\n\n";
		$i++;
	}

	return 0;
}

function makePermalink($url, $randomVal) {
	global $SHREDDER_URL;
	global $SHREDDER_TITLE;
	$atag = "<div class='permalink'><a href='" . $SHREDDER_URL . $url . "&srand=" .$randomVal.  "&frame=n" . "' title='Permalink Shredder $url' onclick='return false;'>$SHREDDER_TITLE</a></div>";
	return $atag;
}

function makeShreddableLink($href, $urlParts) {
	global $SHREDDER_URL;
	$fullHref = makeFullHref($href, $urlParts);
	$atag = '';
	if (strlen($fullHref) > 0) {
		$atag = "<a href='" . $SHREDDER_URL . $fullHref . "'>Shred it</a>";
	}
	return $atag;		
}

function randint($min, $max) {
	return intval(mt_rand($min, $max));
}

// Get the input (url to display)
function getUserUrl() {
	$url = getParam('url');
	if (isset($url)) {
		return $url;
	}
	return '';
}

function showFrame() {
	return !(strtolower(getParam('frame')) == 'n');
}

function startHTMLpage() {
global $SHREDDER_TITLE;
global $SHREDDER_URL;
global $SHREDDER_WIDTH_PIXELS;
$top_display = showFrame() ? 'table-row' : 'none';
$frame = showFrame() ? 'y' : 'n';

$html = <<<EOT
	<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
	<html>
	<head>
	<title>$SHREDDER_TITLE</title>
	<script>
		function shred(url) {
			if (parent.Stoolbar) {
				parent.Stoolbar.loadPage(url);
			}
			else {
				document.location.href='$SHREDDER_URL' + url + '&frame=$frame'; 
			}
		}
		function setShredderTitle(title) {
			document.title='$SHREDDER_TITLE: ' + title;
		}
		function setShredderScale() {
			var width = window.innerWidth || document.body.clientWidth;
			var scale = width/$SHREDDER_WIDTH_PIXELS;
			var el = document.getElementById('shredder-out');
			el.style.transformOrigin = 'left top'; 
			el.style.transform = 'scale(' + scale + ')';
		}
		function setShredderLocation(url) {
			if (url && (input=document.getElementById('shred-url-input'))) {
				input.value = url;
			}
		}
	</script>
	<style>
		html, body {
			height: 100%;
			width: 100%;
		}
		body {
			margin: 0;
			padding: 0;
			background-color: #fefefe;
		}
		.verylargetext {
			letter-spacing: 6px;
			font-size: 94px;
			line-height: 27px;
		}
		.monospacetext {
			font-size: 18px;
			font-family: "Courier New", Courier, monospace;
		}
		.smalltext {
			/* the original shredder default font size (-2) is equivalent to 10px (x-small) */
			font-family: 'arial';
			font-size: 10px;
			line-height: 1.2;
			margin-top: 0;
		}
		.permalink {
			position: absolute;
			left: 700px;
			top: 0px;
		}
		.permalink a {
			font-family: 'arial';
			font-size: 8px;
			text-decoration: none;
			color: #ccc;
		}
		.permalink a:hover {
			text-decoration: underline;
			color: #ccc;
		}
		#outer-container {
			display: table;
			height: 100%;
			width: 100%;
			overflow: hidden;
		}
		#top {
			display: $top_display;
			margin: 0;
			padding: 0;
			overflow: hidden;
		}
		#shredder-top {
			display: block;
			font-family: Calibri, Arial, sans-serif;
			font-size: 18px;
			font-weight: bold;
			padding: 6px 0 6px 16px;
			margin-bottom: 0;
			background-color: #ddd;
			border-top: 1px solid #eee;
			border-bottom: 1px solid #bbb;
			z-index:1000;
			overflow: hidden;
		}
		#shredder-top .logo {
			font-style: italic;
		}
		#shredder-top form { 
			margin-top: 6px;
			margin-bottom: 6px;
		}
		#shredder-top form input {
			padding: 3px;
			margin-right: 4px;
			font-size: 14px;
		}
		#bottom {
			display: table-row;
			margin: 0;
			padding: 0;
			height: 100%;
			overflow: hidden;
		}
		#scroller {
			display: block;
			margin: 0;
			padding: 0;
			height: 100%;
			width: 100%;
			overflow: auto;
		}
		#shredder-out {
			position: relative;
			margin: 2px 0px 0px 2px;
			width: 2000px;
			height: 4000px;
			overflow: hidden;
		}
	</style>
	</head>
	<body>
EOT;
print $html;
}

function endHTMLpage() {
$html = <<<EOT
	<script>
		if (parent.Stoolbar) { parent.Stoolbar.pageLoaded(); }
	</script>
	</body>
	</html>
EOT;
print $html;
}

function startFrame($url) {
	global $SHREDDER_URL_ROOT;
$html = <<<EOT
	<div id="outer-container">
		<div id="top">
			<div id="shredder-top">
				<form action="$SHREDDER_URL_ROOT" method="post">
					<span class='logo'>SHRED</span>:
					<input type="text" name="url" id="shred-url-input" value="$url" style="width:800px" onfocus="this.select();">
					<input type="submit" value="GO">
				</form>
				<script>document.getElementById("shred-url-input").focus();</script>
			</div>
		</div>
		<div id="bottom">
			<div id="scroller">
EOT;
print showFrame() ? $html : '';
}

function endFrame() {
$html = <<<EOT
			</div> <!-- scroller -->
		</div> <!-- bottom -->
	</div> <!-- outer-container -->
EOT;
print showFrame() ? $html : '';
}

// Show message and exit
function errorExit($message)
{
	print "<div>\n";
	print "<h3>Error: $message</h3>\n";
	print "<p><a href='javascript:history.back()'>&lt; BACK</a></p>\n";
	print "</div>\n";
	print "<script>if(parent.Stoolbar){parent.Stoolbar.pageLoaded();}</script>\n";
	print "</body></html>\n";
	exit(-1);
}

?>