/* Load this script using conditional IE comments if you need to support IE 7 and IE 6. */

window.onload = function() {
	function addIcon(el, entity) {
		var html = el.innerHTML;
		el.innerHTML = '<span style="font-family: \'Elusive-Icons\'">' + entity + '</span>' + html;
	}
	var icons = {
			'icon-zoom-out' : '&#xf05c;',
			'icon-zoom-in' : '&#xe000;',
			'icon-youtube' : '&#xe001;',
			'icon-wrench-alt' : '&#xe002;',
			'icon-wrench' : '&#xe003;',
			'icon-wordpress' : '&#xe004;',
			'icon-wheelchair' : '&#xe005;',
			'icon-website-alt' : '&#xe006;',
			'icon-website' : '&#xe007;',
			'icon-warning-sign' : '&#xe008;',
			'icon-w3c' : '&#xe009;',
			'icon-volume-up' : '&#xe00a;',
			'icon-volume-off' : '&#xe00b;',
			'icon-volume-down' : '&#xe00c;',
			'icon-vkontakte' : '&#xe00d;',
			'icon-vimeo' : '&#xe00e;',
			'icon-view-mode' : '&#xe00f;',
			'icon-video-chat' : '&#xe010;',
			'icon-video-alt' : '&#xe011;',
			'icon-video' : '&#xe012;',
			'icon-viadeo' : '&#xe013;',
			'icon-user' : '&#xe014;',
			'icon-upload' : '&#xe015;',
			'icon-unlock-alt' : '&#xe016;',
			'icon-unlock' : '&#xe017;',
			'icon-universal-access' : '&#xe018;',
			'icon-twitter' : '&#xe019;',
			'icon-tumblr' : '&#xe01a;',
			'icon-trash-alt' : '&#xe01b;',
			'icon-trash' : '&#xe01c;',
			'icon-torso' : '&#xe01d;',
			'icon-tint' : '&#xe01e;',
			'icon-time-alt' : '&#xe01f;',
			'icon-time' : '&#xe020;',
			'icon-thumbs-up' : '&#xe021;',
			'icon-thumbs-down' : '&#xe022;',
			'icon-th-list' : '&#xe023;',
			'icon-th-large' : '&#xe024;',
			'icon-th' : '&#xe025;',
			'icon-text-width' : '&#xe026;',
			'icon-text-height' : '&#xe027;',
			'icon-tasks' : '&#xe028;',
			'icon-tags' : '&#xe029;',
			'icon-tag' : '&#xe02a;',
			'icon-stumbleupon' : '&#xe02b;',
			'icon-stop-alt' : '&#xe02c;',
			'icon-stop' : '&#xe02d;',
			'icon-step-forward' : '&#xe02e;',
			'icon-step-backward' : '&#xe02f;',
			'icon-star-empty' : '&#xe030;',
			'icon-star-alt' : '&#xe031;',
			'icon-star' : '&#xe032;',
			'icon-stackoverflow' : '&#xe033;',
			'icon-spotify' : '&#xe034;',
			'icon-speaker' : '&#xe035;',
			'icon-soundcloud' : '&#xe036;',
			'icon-smiley-alt' : '&#xe037;',
			'icon-smiley' : '&#xe038;',
			'icon-slideshare' : '&#xe039;',
			'icon-skype' : '&#xe03a;',
			'icon-signal' : '&#xe03b;',
			'icon-shopping-cart-sign' : '&#xe03c;',
			'icon-shopping-cart' : '&#xe03d;',
			'icon-share-alt' : '&#xe03e;',
			'icon-share' : '&#xe03f;',
			'icon-search-alt' : '&#xe040;',
			'icon-search' : '&#xe041;',
			'icon-screenshot' : '&#xe042;',
			'icon-screen-alt' : '&#xe043;',
			'icon-screen' : '&#xe044;',
			'icon-rss' : '&#xe045;',
			'icon-road' : '&#xe046;',
			'icon-reverse-alt' : '&#xe047;',
			'icon-retweet' : '&#xe048;',
			'icon-return-key' : '&#xe049;',
			'icon-resize-vertical' : '&#xe04a;',
			'icon-resize-small' : '&#xe04b;',
			'icon-resize-horizontal' : '&#xe04c;',
			'icon-resize-full' : '&#xe04d;',
			'icon-repeat-alt' : '&#xe04e;',
			'icon-repeat' : '&#xe04f;',
			'icon-remove-sign' : '&#xe050;',
			'icon-remove-circle' : '&#xe051;',
			'icon-remove' : '&#xe052;',
			'icon-refresh' : '&#xe053;',
			'icon-reddit' : '&#xe054;',
			'icon-record' : '&#xe055;',
			'icon-random' : '&#xe056;',
			'icon-quotes-alt' : '&#xe057;',
			'icon-quotes' : '&#xe058;',
			'icon-question-sign' : '&#xe059;',
			'icon-question' : '&#xe05a;',
			'icon-qrcode' : '&#xe05b;',
			'icon-print' : '&#xe05c;',
			'icon-plus-sign' : '&#xe05d;',
			'icon-plus' : '&#xe05e;',
			'icon-play-circle' : '&#xe05f;',
			'icon-play-alt' : '&#xe060;',
			'icon-play' : '&#xe061;',
			'icon-plane' : '&#xe062;',
			'icon-pinterest' : '&#xe063;',
			'icon-picture' : '&#xe064;',
			'icon-picasa' : '&#xe065;',
			'icon-photo-alt' : '&#xe066;',
			'icon-photo' : '&#xe067;',
			'icon-phone-alt' : '&#xe068;',
			'icon-phone' : '&#xe069;',
			'icon-person' : '&#xe06a;',
			'icon-pencil-alt' : '&#xe06b;',
			'icon-pencil' : '&#xe06c;',
			'icon-pause-alt' : '&#xe06d;',
			'icon-pause' : '&#xe06e;',
			'icon-path' : '&#xe06f;',
			'icon-paper-clip-alt' : '&#xe070;',
			'icon-paper-clip' : '&#xe071;',
			'icon-ok-sign' : '&#xe072;',
			'icon-ok-circle' : '&#xe073;',
			'icon-ok' : '&#xe074;',
			'icon-off' : '&#xe075;',
			'icon-network' : '&#xe076;',
			'icon-myspace' : '&#xe077;',
			'icon-music' : '&#xe078;',
			'icon-move' : '&#xe079;',
			'icon-minus-sign' : '&#xe07a;',
			'icon-minus' : '&#xe07b;',
			'icon-mic-alt' : '&#xe07c;',
			'icon-mic' : '&#xe07d;',
			'icon-map-marker-alt' : '&#xe07e;',
			'icon-map-marker' : '&#xe07f;',
			'icon-male' : '&#xe080;',
			'icon-magnet' : '&#xe081;',
			'icon-magic' : '&#xe082;',
			'icon-lock-alt' : '&#xe083;',
			'icon-lock' : '&#xe084;',
			'icon-livejournal' : '&#xe085;',
			'icon-list-alt' : '&#xe086;',
			'icon-list' : '&#xe087;',
			'icon-linkedin' : '&#xe088;',
			'icon-link' : '&#xe089;',
			'icon-lines' : '&#xe08a;',
			'icon-leaf' : '&#xe08b;',
			'icon-lastfm' : '&#xe08c;',
			'icon-laptop-alt' : '&#xe08d;',
			'icon-laptop' : '&#xe08e;',
			'icon-key' : '&#xe08f;',
			'icon-italic' : '&#xe090;',
			'icon-iphone-home' : '&#xe091;',
			'icon-instagram' : '&#xe092;',
			'icon-info-sign' : '&#xe093;',
			'icon-indent-right' : '&#xe094;',
			'icon-indent-left' : '&#xe095;',
			'icon-inbox-box' : '&#xe096;',
			'icon-inbox-alt' : '&#xe097;',
			'icon-inbox' : '&#xe098;',
			'icon-idea-alt' : '&#xe099;',
			'icon-idea' : '&#xe09a;',
			'icon-home-alt' : '&#xe09b;',
			'icon-home' : '&#xe09c;',
			'icon-heart-empty' : '&#xe09d;',
			'icon-heart-alt' : '&#xe09e;',
			'icon-heart' : '&#xe09f;',
			'icon-hearing-impaired' : '&#xe0a0;',
			'icon-headphones' : '&#xe0a1;',
			'icon-hdd' : '&#xe0a2;',
			'icon-hand-up' : '&#xe0a3;',
			'icon-hand-right' : '&#xe0a4;',
			'icon-hand-left' : '&#xe0a5;',
			'icon-hand-down' : '&#xe0a6;',
			'icon-guidedog' : '&#xe0a7;',
			'icon-group-alt' : '&#xe0a8;',
			'icon-group' : '&#xe0a9;',
			'icon-graph-alt' : '&#xe0aa;',
			'icon-graph' : '&#xe0ab;',
			'icon-googleplus' : '&#xe0ac;',
			'icon-globe-alt' : '&#xe0ad;',
			'icon-globe' : '&#xe0ae;',
			'icon-glasses' : '&#xe0af;',
			'icon-glass' : '&#xe0b0;',
			'icon-github-text' : '&#xe0b1;',
			'icon-github' : '&#xe0b2;',
			'icon-gift' : '&#xe0b3;',
			'icon-fullscreen' : '&#xe0b4;',
			'icon-friendfeed-rect' : '&#xe0b5;',
			'icon-friendfeed' : '&#xe0b6;',
			'icon-foursquare' : '&#xe0b7;',
			'icon-forward-alt' : '&#xe0b8;',
			'icon-forward' : '&#xe0b9;',
			'icon-fork' : '&#xe0ba;',
			'icon-fontsize' : '&#xe0bb;',
			'icon-font' : '&#xe0bc;',
			'icon-folder-sign' : '&#xe0bd;',
			'icon-folder-open' : '&#xe0be;',
			'icon-folder-close' : '&#xe0bf;',
			'icon-folder' : '&#xe0c0;',
			'icon-flickr' : '&#xe0c1;',
			'icon-flag-alt' : '&#xe0c2;',
			'icon-flag' : '&#xe0c3;',
			'icon-fire' : '&#xe0c4;',
			'icon-filter' : '&#xe0c5;',
			'icon-film' : '&#xe0c6;',
			'icon-file-new-alt' : '&#xe0c7;',
			'icon-file-new' : '&#xe0c8;',
			'icon-file-edit-alt' : '&#xe0c9;',
			'icon-file-edit' : '&#xe0ca;',
			'icon-file-alt' : '&#xe0cb;',
			'icon-file' : '&#xe0cc;',
			'icon-female' : '&#xe0cd;',
			'icon-fast-forward' : '&#xe0ce;',
			'icon-fast-backward' : '&#xe0cf;',
			'icon-facetime-video' : '&#xe0d0;',
			'icon-facebook' : '&#xe0d1;',
			'icon-eye-open' : '&#xe0d2;',
			'icon-eye-close' : '&#xe0d3;',
			'icon-exclamation-sign' : '&#xe0d4;',
			'icon-error-alt' : '&#xe0d5;',
			'icon-error' : '&#xe0d6;',
			'icon-envelope-alt' : '&#xe0d7;',
			'icon-envelope' : '&#xe0d8;',
			'icon-eject' : '&#xe0d9;',
			'icon-edit' : '&#xe0da;',
			'icon-dribbble' : '&#xe0db;',
			'icon-download-alt' : '&#xe0dc;',
			'icon-download' : '&#xe0dd;',
			'icon-digg' : '&#xe0de;',
			'icon-deviantart' : '&#xe0df;',
			'icon-delicious' : '&#xe0e0;',
			'icon-dashboard' : '&#xe0e1;',
			'icon-css' : '&#xe0e2;',
			'icon-credit-card' : '&#xe0e3;',
			'icon-compass-alt' : '&#xe0e4;',
			'icon-compass' : '&#xe0e5;',
			'icon-comment-alt' : '&#xe0e6;',
			'icon-comment' : '&#xe0e7;',
			'icon-cogs' : '&#xe0e8;',
			'icon-cog-alt' : '&#xe0e9;',
			'icon-cog' : '&#xe0ea;',
			'icon-cloud-alt' : '&#xe0eb;',
			'icon-cloud' : '&#xe0ec;',
			'icon-circle-arrow-up' : '&#xe0ed;',
			'icon-circle-arrow-right' : '&#xe0ee;',
			'icon-circle-arrow-left' : '&#xe0ef;',
			'icon-circle-arrow-down' : '&#xe0f0;',
			'icon-child' : '&#xe0f1;',
			'icon-chevron-up' : '&#xe0f2;',
			'icon-chevron-right' : '&#xe0f3;',
			'icon-chevron-left' : '&#xe0f4;',
			'icon-chevron-down' : '&#xe0f5;',
			'icon-check-empty' : '&#xe0f6;',
			'icon-check' : '&#xe0f7;',
			'icon-certificate' : '&#xe0f8;',
			'icon-cc' : '&#xe0f9;',
			'icon-caret-up' : '&#xe0fa;',
			'icon-caret-right' : '&#xe0fb;',
			'icon-caret-left' : '&#xe0fc;',
			'icon-caret-down' : '&#xe0fd;',
			'icon-car' : '&#xe0fe;',
			'icon-camera' : '&#xe0ff;',
			'icon-calendar-sign' : '&#xe100;',
			'icon-calendar' : '&#xe101;',
			'icon-bullhorn' : '&#xe102;',
			'icon-bulb' : '&#xe103;',
			'icon-brush' : '&#xe104;',
			'icon-broom' : '&#xe105;',
			'icon-briefcase' : '&#xe106;',
			'icon-braille' : '&#xe107;',
			'icon-bookmark-empty' : '&#xe108;',
			'icon-bookmark' : '&#xe109;',
			'icon-book' : '&#xe10a;',
			'icon-bold' : '&#xe10b;',
			'icon-blogger' : '&#xe10c;',
			'icon-blind' : '&#xe10d;',
			'icon-bell' : '&#xe10e;',
			'icon-behance' : '&#xe10f;',
			'icon-barcode' : '&#xe110;',
			'icon-ban-circle' : '&#xe111;',
			'icon-backward' : '&#xe112;',
			'icon-asterisk' : '&#xe113;',
			'icon-asl' : '&#xe114;',
			'icon-arrow-up' : '&#xe115;',
			'icon-arrow-right' : '&#xe116;',
			'icon-arrow-left' : '&#xe117;',
			'icon-arrow-down' : '&#xe118;',
			'icon-align-right' : '&#xe119;',
			'icon-align-left' : '&#xe11a;',
			'icon-align-justify' : '&#xe11b;',
			'icon-align-center' : '&#xe11c;',
			'icon-adult' : '&#xe11d;',
			'icon-adjust-alt' : '&#xe11e;',
			'icon-adjust' : '&#xe11f;',
			'icon-address-book-alt' : '&#xe120;',
			'icon-address-book' : '&#xe121;'
		},
		els = document.getElementsByTagName('*'),
		i, attr, c, el;
	for (i = 0; ; i += 1) {
		el = els[i];
		if(!el) {
			break;
		}
		attr = el.getAttribute('data-icon');
		if (attr) {
			addIcon(el, attr);
		}
		c = el.className;
		c = c.match(/icon-[^\s'"]+/);
		if (c && icons[c[0]]) {
			addIcon(el, icons[c[0]]);
		}
	}
};