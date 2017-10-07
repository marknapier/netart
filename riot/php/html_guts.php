<?php

$USER_AGENT_STR = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36";

function getHeaders($url) {
	global $USER_AGENT_STR ;

    $ch = curl_init();

    // return headers only
    curl_setopt($ch, CURLOPT_HEADER, true);
	curl_setopt($ch, CURLOPT_NOBODY, true);
	// spoof a valid browser
    curl_setopt($ch, CURLOPT_USERAGENT, $USER_AGENT_STR);
    //return the transfer as a string
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
    // https
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    // follow redirects
    curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
    curl_setopt($ch, CURLOPT_MAXREDIRS, 10);
    // set url
    curl_setopt($ch, CURLOPT_URL, $url);

    // retrieve the headers
    $headers = curl_exec($ch);

    // get redirected url, if any
    $matches = array();
    preg_match_all('/^Location:(.*)$/mi', $headers, $matches);
    $location = !empty($matches[1]) ? trim($matches[1][0]) : $url;

    // get the http headers and response code
    $results = array(
    	'url' => $url,
    	'location' => $location, // this will be the actual url of the final page loaded
    	'headerText' => $headers,
        'httpCode' => curl_getinfo($ch, CURLINFO_HTTP_CODE)
	);
    curl_close($ch);
    return $results;
}

function getHeadersLast($url) {
	global $USER_AGENT_STR ;
	$redirects = 0;
	$finalURL = false;

	while (!$finalURL && $redirects < 10) {
		$headerInfo = getHeaders($url);
		$httpCode = $headerInfo['httpCode'];

	    // guts("getHeaderLast: headerInfo is ", $headerInfo);

	    if ($httpCode == 200) {
	    	// found page: return it
	    	$finalURL = $url;
	    }
	    else if ($httpCode == 404) {
	    	// page not found: give up
	    	errorExit("<p>HTTP returned 404: file not found<BR>\n <BR> <a href='javascript:history.back()'>&lt; BACK</a></p>");
	    }
		else if ($httpCode == 301 || $httpCode == 302) {
			// redirect to another url
			$url = getRedirectURL($headerInfo['headerText']);
			// TEST and Sanitze URL
	        $url_parsed = parse_url($url);
	    	// guts("getHeaderLast:  url_parsed is ", $url_parsed);
	        if (!isset($url_parsed)) {
	        	errorExit("<p>Redirect found a funky url<BR>\n <a href='javascript:history.back()'>&lt; BACK</a></p>");
	        }
			$redirects++;
	    }
	    else {
	    	errorExit("<p>HTTP returned error code: $httpCode<BR>\n <BR> <a href='javascript:history.back()'>&lt; BACK</a></p>");
	    }
	}
	if (!$finalURL) {
		errorExit("TOO MANY redirects!!!!");
	}
    return $finalURL;
}

function getRedirectURL($headerText) {
	list($header) = explode("\r\n\r\n", $headerText, 2);

	// guts("getREDIRECTURL: header is ", $header);

	$matches = array();
	preg_match("/(Location:|URI:)[^(\n)]*/", $header, $matches);
	$url = trim(str_replace($matches[1], "", $matches[0]));
    return $url;
}

function getHTML($url) {
	global $USER_AGENT_STR ;
	$numRedirects = 0;

    // create curl resource 
    $ch = curl_init();

    // Set options:
    curl_setopt($ch, CURLOPT_FOLLOWLOCATION, true);
    curl_setopt($ch, CURLOPT_MAXREDIRS, 10);
    curl_setopt($ch, CURLOPT_USERAGENT, $USER_AGENT_STR);
	curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);   // return the results as a string

    // set url 
    curl_setopt($ch, CURLOPT_URL, $url);

    // return the html 
    $output = curl_exec($ch); 
    // guts('HTMLHTMLHTMLHTML 0', $output);

    curl_close($ch);
    return $output;
}

function makeFullHref($href, $urlParts) {
	$fullHref = '';
	if (strlen($href) > 0) {
		if (hasProtocol($href)) {
			$fullHref .= $href;		// href is complete
		}
		else if ($href[0] == '/' && strlen($href) > 1 && $href[1] == '/') {
			$fullHref .= $urlParts['scheme'] . ':' . $href;   // href starts with '//'
		}
		else if ($href[0] == '/') {
			$fullHref .= catURL($urlParts['scheme'] . '://' . $urlParts['host'], $href);   // href is relative to root
		}
		else if ($href[0] == '.' && $href[1] == '/') {
			$fullHref .= catURL($urlParts['base'], substr($href, 2));   // href is relative to html file
		}
		else {
			$fullHref .= catURL($urlParts['base'], $href);   // href is relative to html file
		}
	}
	return $fullHref;		
}

function makePlainLink($href, $urlParts) {
	$fullHref = makeFullHref($href, $urlParts);
	$atag = '';
	if (strlen($fullHref) > 0) {
		$atag = "<a href='" . $fullHref . "'>Show it</a>";
	}
	return $atag;		
}

function catURL($hostPath, $relativeHref) {
	if (substr($hostPath, -1) != '/' && $relativeHref[0] != '/') {
		$hostPath .= '/';
	}
	else if (substr($hostPath, -1) == '/' && $relativeHref[0] == '/') {
		$hostPath = rtrim($hostPath, '/');
	}
	return $hostPath . $relativeHref;
}

function getHTMLElements($html, $url) {
	$urlParts = testURL($url);
	$elements = array();
	$links = array();
	$linkTexts = array();
	$images = array();
	$videos = array();
	$mp4s = array();
	$fonts = array();
	$font_batches = array();
	$colors = array();
    $color_batches = array();
    $frames = array();
    $title = "";
    $baseHref = null;
    $bodyBGColor = null;
    $bodyBGImage = null;

	# Create a DOM parser object
	$dom = new DOMDocument();

	# Parse the HTML
	# The @ before the method call suppresses any warnings that
	# loadHTML might throw because of invalid HTML in the page.
	@$dom->loadHTML($html);

	# <body> tag
	foreach($dom->getElementsByTagName('body') as $bodyTag) {
		$bodyBGColor = $bodyTag->getAttribute('bgcolor');
		$bodyBGImage = $bodyTag->getAttribute('background');
	}

	# <title> tag
	foreach($dom->getElementsByTagName('title') as $titleTag) {
		$title = $titleTag->nodeValue;
	}

	# <base href> tag
	foreach($dom->getElementsByTagName('base') as $baseTag) {
		$baseHref = $baseTag->getAttribute('href');
	}

	# <a> tags
	foreach($dom->getElementsByTagName('a') as $link) {
		$href = $link->getAttribute('href');
        if (strlen($href) > 0 && $href[0] != '#') {
	        // print "A HREF=" . $href;
	        // print " NAME=" . $link->nodeValue;
	        // print " " . makeShreddableLink($href, $urlParts);
	        // print "<br />";
        	array_push($links, $link->getAttribute('href'));
        	array_push($linkTexts, $link->nodeValue);
        }
	}

	# <img> tags
	foreach($dom->getElementsByTagName('img') as $img) {
        // print "IMG SRC=" . $img->getAttribute('src');
        // print " " . makePlainLink($img->getAttribute('src'), $urlParts);
        // print "<br />";
        array_push($images, $img->getAttribute('src'));
        if (  $img->getAttribute('data-src-medium')    ) {
        	// print "DATA-SOURCE SRC=" . $img->getAttribute('src');
        	array_push( $images, $img->getAttribute('data-src-medium')  );
        }
	}

	# <style> tags
	foreach($dom->getElementsByTagName('style') as $style) {
        // styles embedded in doc
        if (isset($style->nodeValue)) {
	        $c = getColors($style->nodeValue);
	        array_push($color_batches, $c);
        }
	}

	# <script> tags, look for location.href commands
	// # MJN oct17,2001
	// # Check for location = <href> code inside script tags
	// # Example: parent.location = "index1.html";
	// $buffref = $buffer;
	// while ( $buffref =~ /\.location\s?=\s?([^ >\r\t\n\f]*)/ig ) {
	// 	$HREF = $1;
	// 	# remove backslashes, ie \', which turns up in scripts
	// 	$HREF =~ s/['">\\\;]//g;
	// 	push(@links,$HREF);
	// 	print "GOT Javascript location=$HREF<BR>\n";
	// }
	// # Example: parent.location.href = "index1.html";
	// $buffref = $buffer;
	// while ( $buffref =~ /\.location.href\s?=\s?([^ >\r\t\n\f]*)/ig ) {
	// 	$HREF = $1;
	// 	# remove backslashes, ie \', which turns up in scripts
	// 	$HREF =~ s/['">\\;]//g;
	// 	push(@links,$HREF);
	// 	print "GOT Javascript location.href=$HREF<BR>\n";
	// }

	# <video> tags
	foreach($dom->getElementsByTagName('video') as $video) {
        // save the entire tag
        $videoTag = $dom->saveXML($video);
		// print "VIDEO TAG IS " . htmlspecialchars($videoTag, ENT_IGNORE) . "\n\n";
	    array_push($videos, $videoTag);
	}

	# MP4 urls
	$mp4urls = getMP4s($html);
	foreach($mp4urls as $mp4url) {
		// print "MP4 url IS " . $mp4url . "\n\n";
	    array_push($mp4s, makeFullHref($mp4url, $urlParts));
	}

	# <link> tags
	foreach($dom->getElementsByTagName('link') as $link) {
        // stylesheet
        if ($link->getAttribute('rel') == 'stylesheet') {
	        $cssText = getHTML( makeFullHref($link->getAttribute('href'), $urlParts) );
	        $c = getColors($cssText);
	        $b = getBGColors($cssText);
	        // guts("BODYBG BODYBG", $b);
	        $f = getFonts($cssText);
	        array_push($color_batches, $c);
	        array_push($font_batches, $f);
        }
	}

	# <frame> tags
	foreach($dom->getElementsByTagName('frame') as $frame) {
		array_push($frames, makeFullHref($frame->getAttribute('src'), $urlParts));
	}

	# <meta refresh> tags
	foreach($dom->getElementsByTagName('meta') as $metatag) {
		$http_equiv = strtolower($metatag->getAttribute("http-equiv"));
		// guts("GOT METATAG", $http_equiv);
		if ($http_equiv == 'refresh') {
			$content = $metatag->getAttribute('content');
			// guts("META CONTENT", $content);
			if (preg_match("/url\s?=\s?([^ ]*)/i", $content, $match)) {
				// guts("MATCHED",$match);
				array_push($frames, makeFullHref($match[1], $urlParts));
			}
		}
	}

	if (sizeof($color_batches) > 0) {
		$colors = call_user_func_array('array_merge', $color_batches);
	}
	if (sizeof($font_batches) > 0) {
		$fonts = call_user_func_array('array_merge', $font_batches);
	}

	$elements['title'] = $title;
	$elements['bodyBGColor'] = $bodyBGColor;
	$elements['bodyBGImage'] = $bodyBGImage;
	$elements['images'] = $images;
	$elements['videos'] = $videos;
	$elements['mp4s'] = $mp4s;
	$elements['links'] = $links;
	$elements['linkTexts'] = $linkTexts;
	$elements['colors'] = $colors;
	$elements['fonts'] = $fonts;
	$elements['text'] = getTextFromHTML($dom);  // extract plain text (non HTML commands)
	$elements['html'] = htmlspecialchars($html, ENT_IGNORE | ENT_COMPAT, 'UTF-8');
	$elements['frames'] = $frames;
	$elements['base'] = $baseHref;

	return $elements;
}

function getMP4s($htmlText) {
	// match:  'blahblahblahblah.mp4'
	$matches = array();
	$num = preg_match_all ('/[\"\']+([^\"\']+\.mp4)[\"\']+/i', $htmlText, $matches);
	return $matches[1];
}

function getColors($cssText) {
	// match:  #0039AF  #09F
	$matches = array();
	$num = preg_match_all ('/#([a-fA-F0-9]){3}(([a-fA-F0-9]){3})?\b/', $cssText, $matches);
	return $matches[0];
}

function getBGColors($cssText) {
	// match:    body { background-color: #fefefe;
	$matches = array();
	$num = preg_match_all ('/body\s*{[a-zA-Z0-9%:!;\s\-_,\"\'\n]*background-color\:\s*(\#[0-9a-f]*)\;/im', $cssText, $matches);
	return $matches[1];
}

function getFonts($cssText) {
	// match font-family: "Times New Roman", serif;
	// return just the font names, not the 'font-family:'
	$matches = array();
	$num = preg_match_all('/font-family\s?\:\s?([^;}]*);/', $cssText, $matches);
	$fontNames = $matches[1];
	$cleanedFontNames = array();

	// remove 'inherit'
	foreach($fontNames as $fontName) {
		if (strpos($fontName, 'inherit') === FALSE) {
			// print "CLEAN font $fontName \n";
			array_push($cleanedFontNames, $fontName);
		}
	}
	return $cleanedFontNames;
}

function getTextFromTags($dom, $tagname, &$text) {
	foreach($dom->getElementsByTagName($tagname) as $t) {
        $tmp = $t->nodeValue;
	    $tmp = preg_replace( "/<([^>]|\n)*?>/", "", $tmp);		# kill html tags
	    $tmp = preg_replace( "/\s+/", " ", $tmp);				# convert newlines and tabs to spaces
        array_push($text, $tmp);
	}
}

function getTextFromHTML($dom) {
	$text = array();
	getTextFromTags($dom, 'H1', $text);
	getTextFromTags($dom, 'H2', $text);
	getTextFromTags($dom, 'H3', $text);
	getTextFromTags($dom, 'H4', $text);
	getTextFromTags($dom, 'H5', $text);
	getTextFromTags($dom, 'p', $text);
	getTextFromTags($dom, 'b', $text);
	getTextFromTags($dom, 'i', $text);
	getTextFromTags($dom, 'font', $text);
	return (sizeof($text) > 0) ? implode(' ', $text) : '';
}

function getTextFromHTML_BREAKS_MEMORY($html) {
	$text = "blah blah blah";
	$matches = array();
	// $charset = (mb_check_encoding($string,'ISO-8859-1')) ? 'ISO-8859-1' : 'UTF-8';

	// print "getTextFromHTML()\n";

	// These long regex matches crashed PHP on windows, due to stack size too small.
	// Added these lines to httpd.conf (Apache server config file) to increase stack for PHP:
	// <IfModule mpm_winnt_module>
	//    ThreadStackSize 8388608
	// </IfModule>
	//
	$matches = explode('<body', $html);

	// if ( preg_match("/<BODY(.|\n)*<\/BODY>/i", $html, $matches) ) {
		print "<!FOUND A BODY TAG>\n";
		$buffer = $matches[1];
		/*
		$buffer = preg_replace( "/<SCRIPT(.|\n)*?<\/SCRIPT>/i", "", $buffer);		# kill scripts
		$buffer = preg_replace( "/<A\sHREF[^>]+>[^<]+<\/A>/i", "", $buffer);		# kill links
		*/
		$buffer = preg_replace( "/<([^>]|\n)*?>/", "", $buffer);					# kill html tags
		$buffer = preg_replace( "/[<>\n\r\t]/", "", $buffer);				# kill newlines and strays
		$buffer = preg_replace( "/\s+/", " ", $buffer);				# kill newlines and strays
		$words = preg_split("/[\s]+/", $buffer);
		print "<!FOUND words=((($buffer)))>\n";
		guts("WORDS", $words);
		for ($i=0; $i < 100; $i++) {
			if ($w = array_shift($words)) {
				$text = $text . " " . $w;
			}
		}
		/* */
	// }
	return $text;
}

function hasProtocol($URLstring) {
	return preg_match('/^https?\:\/\//', $URLstring);
}

function testURL($URLstring) {
	// ([^:/?#.]+)\.([A-Za-z][A-Za-z])      match domain
	if (!hasProtocol($URLstring)) {
		$URLstring = 'http://' . $URLstring;
	}

	if (filter_var($URLstring, FILTER_VALIDATE_URL) === FALSE) {
		return FALSE;
	}

	$urlParts = parse_url($URLstring);

	if (isset($urlParts['host'])) {
		if (isset($urlParts['path'])) {
			$path = $urlParts['path'];
			$subdir = '';
			$file = '';
			// match /some/path/filename.html
			preg_match("/^(\/.*)(\/.*\..*)$/", $path, $match);

			// guts("testURL: subdir path ($path) match", $match);

			if (sizeof($match) == 3) {  # got subdirs and filename
				$subdir = $match[1];
				$file = $match[2];
			} else {
				// match /filename.html
				if (preg_match("/^(\/.*\..*)$/", $path, $match)) {
					$subdir = '';
					$file = $match[1];
				}
				else {
					$subdir = $path;
					$file = '';
				}
			}
			$urlParts['subdir'] = $subdir;
			$urlParts['file'] = $file;
		}
		else {
			$urlParts['path'] = '';
			$urlParts['subdir'] = '';
		}

		$urlParts['base'] = $urlParts['scheme'] . '://' . $urlParts['host'] . $urlParts['subdir'];

		// guts("TestURL: urlparts", $urlParts);
	}
	else {
		return FALSE;
	}

	return $urlParts;
}

function buildURL($urlParts) {
	$up = $urlParts;
	$url = $up['scheme'] . '://' . $up['host'] . $up['path'];
	if (isset($up['fragment'])) {
		$url = $url . '#' . $up['fragment'];	
	}
	if (isset($up['query'])) {
		$url = $url . '?' . $up['query'];	
	}
	return $url;
}

function guts($title, $blah) {
	print "<h3>$title</h3><pre>\n";
	var_dump($blah);
	print "</pre>\n";
}

function filterchars($str) {
	// strip whitespace, allow only legal chars
	$str = preg_replace('/\s\s+/', '', $str);
	$str = preg_replace('/[^0-9a-zA-Z\.\-\_\/\?\=]/', '', $str);
	return $str;
}

function sanitizeUrl($URL) {
	// strip any char not listed below (for example ' " % < > )
    $str = preg_replace('/[^-A-Za-z0-9+&@#\&\/\?=~_|!:,.;\(\)]/', '', $URL);
    return $str;
}

// Get the input (url to display)
function getParam($name) {
	$param = null;
	if (isset($_POST[$name])) {
		$param = $_POST[$name];
	}
	else if (isset($_GET[$name])) {
		$param = $_GET[$name];
	}
	return $param;
}

function mergeElements($elements1, $elements2) {
	$result = array();
	$result['title'] 		= $elements1['title'] . $elements2['title'];
	$result['bodyBGColor'] 	= $elements2['bodyBGColor'];
	$result['base'] 		= $elements2['base'];
	$result['html'] 		= $elements1['html'] . $elements2['html'];
	$result['images'] 		= array_merge($elements1['images'], $elements2['images']);
	$result['videos'] 		= array_merge($elements1['videos'], $elements2['videos']);
	$result['mp4s'] 		= array_merge($elements1['mp4s'], $elements2['mp4s']);
	$result['links'] 		= array_merge($elements1['links'], $elements2['links']);
	$result['linkTexts'] 	= array_merge($elements1['linkTexts'], $elements2['linkTexts']);
	$result['colors'] 		= array_merge($elements1['colors'], $elements2['colors']);
	$result['fonts'] 		= array_merge($elements1['fonts'], $elements2['fonts']);
	$result['frames'] 		= array_merge($elements1['frames'], $elements2['frames']);
	return $result;
}

?>