$( document ).ready(function() {
	$("#dialog").dialog({
		autoOpen : false, 
		modal : true, 
		show : "blind", 
		hide : "blind",
		buttons: {
            'Yes': function(){
                $(this).dialog('close');
                callback("yes");
            },
            'No': function(){
                $(this).dialog('close');
                callback("no");
            }
        },
        closeOnEscape: false,
        draggable: false,
        resizable: false
	});
	
	//initialise servers from Java
	var apps = JSON.parse(java.getServerConfigListAsJson());
	//initialise plugins
	
	//get current machine JVM for settings tabs
	var currentJvm = java.getJava();

	//initialise variables
	var newServerBool = false;	
	var selectedServer = 0;
	var newServer = 0;
	var servers = [];
	var defaultHost = "127.0.0.1";
	
	//initialise html for cloning
	var editTemplate = $('.settings').clone().html();
	var consoleFooterTemplate = $('.console-info').clone().html();
	var settingsFooterTemplate = $('#settings_footer').clone().html();
	var headerTemplate = $('.header').clone().html();
	var pluginListTemplate = $('.plugin_template').clone().html();
	var jettyVersionTemplate = $('.jetty_version').clone().removeClass('hide');
	
	//get and list saved webapps
	refreshServerList();
	refreshEditFormsAndConsoles();
	
	$('.noapps').removeClass('hide');
	var v = java.getJettyVersion();
	jettyVersionTemplate.html(jettyVersionTemplate.html().replace("{j}", v));	
	$('.jetty_version').after(jettyVersionTemplate);
	
	if (apps == "") {
		showNoAppsMsg();
	}
	else {
		showClickAppMsg();
	}
	
	//on click functions
	$(document).on('click', '.j_info', function() {
		$('body, .header').toggleClass('slidedown');
	});
	
	$(document).on('click', '.github_link', function(){
		java.goToGithub();
	});
	
	$(document).on('click', '#noappsButton', function() {
		$('.noapps').addClass('hide');showClickAppMsg
		addWebApp();
	});
	
	$(document).on('click', '.action', function(e) {
		$('.header').removeClass('hide');
		var id = $(this).closest('a').attr('id').split('_')[1];
		selectedServer = id;
		if ($(this).closest('a').hasClass('running')){
			stopServer(id);		
			e.stopPropagation();
		}
		else {
			startServer(id);
			e.stopPropagation();
		}
	});	

	$(document).on('click', '.j_settings', function() {
		$( '#settings_footer, .settings, #btn_save_' + selectedServer + ', #btn_delete_' + selectedServer + ', #edit_' + selectedServer + ', .settings' ).removeClass( 'hide' );
    	$( '#console_footer, .plugin, #console_template, #console_' + selectedServer ).addClass( 'hide' );
    	$('.noapps').addClass('hide');
    	
    	$( '.tab' ).removeClass( 'active' );
		$( '.j_settings' ).addClass( 'active' );

		//get current webapp from list
    	$( '.current' ).each(function( index ) {
    		var tagid = $(this).attr('id');
        	selectedServer = tagid.split('_')[1];
    	});

    	if (!java.getRunning(selectedServer)){
    		$( '#btn_delete_' + selectedServer ).prop( 'disabled', false );
    		$( '#btn_save_' + selectedServer ).prop( 'disabled', false );
    	}
    	else if (java.getRunning(selectedServer)){
    		$( '#btn_delete_' + selectedServer ).prop( 'disabled', true );
    		$( '#btn_save_' + selectedServer ).prop( 'disabled', true );
    	}

    	//display current edit window
    	$( '#edit_' + selectedServer ).removeClass('hide');
	});
	
	$(document).on('click', '.j_console', function() {
		$( '.current' ).each(function( index ) {
			var tagid = $(this).attr('id');
	    	selectedServer = tagid.split('_')[1];
		});
		$('.noapps').addClass('hide');
		$( '.settings, .plugin, #settings_footer, #edit_' + selectedServer).addClass( 'hide' );
		$( '#console_footer, #btn_clear, #console_' + selectedServer ).removeClass( 'hide' );
		$( '.tab' ).removeClass( 'active' );
		$( '.j_console' ).addClass( 'active' );
				
		if (!java.getRunning(selectedServer)){
			$( '#btn_delete_' + selectedServer ).prop( 'disabled', false );
    		$( '#btn_save_ ' + selectedServer ).prop( 'disabled', false );
    		
    		$( '#btn_clear' + selectedServer ).prop( 'disabled', true );
    		$( '#btn_open ' + selectedServer ).prop( 'disabled', true );
    	}
    	else if (java.getRunning(selectedServer)){
    		$( '#btn_delete_' + selectedServer ).prop( 'disabled', true );
    		$( '#btn_save_ ' + selectedServer ).prop( 'disabled', true );
    		
    		$( '#btn_clear' + selectedServer ).prop( 'disabled', false );
    		$( '#btn_open ' + selectedServer ).prop( 'disabled', false );
    	}
	
		//display current edit window
		$( '#edit_' + selectedServer ).removeClass('hide');
	});
	
    $(document).on('click', '.app', function() {
    	var currentTab = $('.tabs').find('.active').attr('id');
    	$( 'body' ).find( '.data' ).addClass('hide');
    	$( 'body' ).find( '.head' ).addClass('hide');
    	$('.tabs').find('.plugin_tab').addClass('hide');
    	$('.header, #btn_clear').removeClass('hide');
    	$('.noapps').addClass('hide');
    	//this.id is "webapp_1"
    	$( '.settings, .plugin' ).addClass( 'hide' );
    	var tagid = this.id;
    	selectedServer = $(this).data('index');
    	
    	$('.tabs').find('a[id$="_' + selectedServer + '"]').removeClass('hide');
    	
    	$( 'body' ).find( '.head' ).addClass('hide');
    	$('#header_' + selectedServer).removeClass('hide');
    	
    	$( this ).addClass('current');
    	//show settings and console tabs
    	if( $( '.j_settings' ).hasClass( 'hide' ) && $( '.j_console' ).hasClass( 'hide' ) ) {
    		$('.j_settings, .j_console').removeClass('hide');
    	}
    	
    	//remove current status, hide console and edit forms from all apps
    	for (var i in apps){
    		var id = apps[i].SERVER_ID;
    		$( '#webapp_' + id ).removeClass('current');
    		$( '#settings, #console_' + id + ',#edit_' + id + ', #memory_' + id + ', #lastupdate_' + id + ', #btn_delete_' + id + ', #btn_save_' + id  ).addClass('hide');
    	}
    	
    	if (newServerBool){
    		$( '#settings, #console_' + newServer + ',#edit_' + newServer + ', #memory_' + newServer + ', #lastupdate_' + newServer + ', #btn_delete_' + newServer + ', #btn_save_' + newServer  ).addClass('hide');
    	}

    	if (!java.getRunning(selectedServer)){
    		$( '#btn_delete_' + id ).prop( 'disabled', false );
    		$( '#btn_open_' + id ).prop( 'disabled', true );
    	}
    	else if (java.getRunning(selectedServer)){
    		$( '#btn_delete_' + id ).prop( 'disabled', true );
    		$( '#btn_open_ ' + id ).prop( 'disabled', false );
    	}
    	
    	//hide template
    	if ( !$('#console_template').hasClass('hide') ) {
    		$('#console_template').addClass('hide');
    	}
    	
    	if ( $( '.j_settings' ).hasClass( 'active' ) ) {
    		
    		$( '.settings, #btn_save_' + selectedServer + ', #btn_delete_' + selectedServer + ', #edit_' + selectedServer).removeClass( 'hide' );
	    	$( '#console_footer, #console_' + selectedServer ).addClass( 'hide' );
    	}
    	else if ( $( '.plugin_tab' ).hasClass( 'active' ) ) {
    		$('#btn_clear').addClass('hide');
    		var seperated = currentTab.split('_');    		
    		$('.tab_' + seperated[1]).addClass('active');
    		var pluginView = "plugin_" + seperated[1] + '_' + selectedServer;//plugin_bluedragondotlog_1
    		$('#' + pluginView).removeClass('hide');
    	}
    	else {    		
    		$( '.j_console' ).addClass( 'active' );  
	    	$( '#settings_footer, #edit_' + selectedServer ).addClass( 'hide' );
	    	$( '#console_footer, #console_' + selectedServer + ', #memory_' + selectedServer + ', #lastupdate_' + selectedServer).removeClass( 'hide' );
	    	if (!java.getRunning(selectedServer)){
	    		$('#btn_start').removeClass('hide');
	    		$('#btn_stop').addClass('hide');
	    	}
	    	else if (java.getRunning(selectedServer)){
	    		$('#btn_start').addClass('hide');
	    		$('#btn_stop').removeClass('hide');
	    	}
    	}

		//highlight current
		$( '#webapp_' + selectedServer ).addClass('current');
		
    });
    
    $(document).on('click', '.addwebapp', function() {
    	$( '.noapps' ).addClass('hide');
    	$( '.console' ).addClass( 'hide' );
    	$( '.app' ).removeClass( 'active' );
    	$( '#console_template' ).removeClass( 'hide' );
    	$( 'body' ).find( '.head' ).addClass('hide');
    	$( 'body, .header' ).removeClass('slidedown');
    	$( '#btn_clear' ).removeClass('hide');
    	addWebApp();
    });

    $(document).on('click', '.j_save', function() {
    	var savedServer = selectedServer;
    	
    	if (newServer != 0){
    		savedServer = newServer;
    	}

    	if (validate(savedServer)){
	    	var name = $('#form_name_' + savedServer).val();
	    	var ip = $('#form_ip_' + savedServer).val();
			var port = $('#form_port_' + savedServer).val();
			var webFolder = $('#form_web_folder_' + savedServer + '_text').val();
			var uri = $('#form_uri_' + savedServer).val();
			var defaultJvm, customJvmBool, customJvm;
	
			if ($(' #form_radio_hotspot_' + savedServer).is(':checked')) {
				defaultJvm = true;
				customJvmBool = false;
				customJvm = "";
			}
			else {
				defaultJvm = false;
				customJvmBool = true;
				customJvm = $(' #form_customjvm_' + savedServer + '_text').val();
			}
	
			var jvmArgs = $(' #form_jvmargs_' + savedServer).val();
			var memory = $(' #form_memory_' + savedServer).val();
	
			//new java
			if (apps.length != servers.length){
				java.saveSettings(true, savedServer, name, ip, port, webFolder, uri, defaultJvm, customJvmBool, customJvm, jvmArgs, memory);
			}
			//existing java
			else {
				java.saveSettings(false, savedServer, name, ip, port, webFolder, uri, defaultJvm, customJvmBool, customJvm, jvmArgs, memory);
			}
	
			updateHtml();
			
			$('#info_name_' + savedServer).text(name);
			if (ip == ""){
				tempIp = defaultHost;
			}
			else {
				tempIp = ip;
			}
			$('#info_host_' + savedServer).text(tempIp + ":" + port);
			$('#info_dir_' + savedServer).text(webFolder);			
			
			newServerBool = false;
			newServer = 0;
			$( '#btn_delete_' + savedServer ).attr( 'disabled', false );
			//re add current class & show console
			if (!selectedServer == 0){
				$( '#webapp_' + selectedServer ).addClass('current');
				$( '#console_' + selectedServer ).removeClass('hide');
				$( '#edit_' + selectedServer ).addClass('hide');
				$( '#console_footer').removeClass('hide');
				$( '#settings_footer').addClass('hide');
				$( '.j_console, .j_settings' ).removeClass('hide');
				$( '.tab' ).removeClass( 'active' );
				$( '.tab' ).addClass( 'hide' );
				$( '.j_console' ).addClass( 'active' );
			}
			else {
				$( '#console_template' ).removeClass('hide');
			}
			//disable edit, delete, start buttons
			$( '#btn_delete_' + savedServer ).attr( 'disabled', false );
			$( '#btn_clear' ).prop( 'disabled', false );			
			$( '.footer' ).addClass('hide');
			$( '.header, #btn_clear' ).removeClass('hide');
			
			orderList();
			
			showClickAppMsg();
			$( '.console' ).addClass( 'hide' );
			$( '#console_template' ).removeClass('hide');
			$( '.noapps' ).removeClass('hide');
			$( '.app' ).removeClass( 'current' );
			
			$( '.plugin_tab' ).remove();
			$( '.plugin' ).remove();
			java.initialisePlugins();
	    }
    });
    
    $(document).on('click', '.j_html', function() { 
    	java.log(document.documentElement.innerHTML);
    });
  
    $(document).on('click', '.start', function(e) {
    	startServer(selectedServer);
		e.stopPropagation();
    });

    $(document).on('click', '.stop', function(e) {
    	stopServer(selectedServer);		
		e.stopPropagation();
    });
    
    $(document).on('click', '#btn_open', function() {
    	$( '.current' ).each(function( index ) {
    		var tagid = $(this).attr('id');
        	selectedServer = tagid.split('_')[1];
    	});
    	
    	for (var i in apps){
    		var id = apps[i].SERVER_ID;
    		if (id == selectedServer){
    			var host = apps[i].SERVER_IP;
    	    	var uri = apps[i].DEFAULTURI;
    	    	
    	    	java.openWebApp(host, uri);
    		}
    	}    	
    });

    $(document).on('click', '#btn_clear', function() {
    	document.getElementById('console_' + selectedServer).innerHTML = "";
    });

    $(document).on('click', '.delete', function() {
    	$( '.tab' ).removeClass( 'active' );
    	
    	$('#dialogText').html('Delete <span id="var">{y}</span>?');
    	$('#dialogText').attr('id', 'dialog_delete');
    	$('#var').text(java.getNameOfApp(selectedServer));
    	$("#dialog").dialog("open");
    });
    
    $(document).on('click', '.select_server', function() {
    	var folder;
    	var serv;
    	
    	var tagid = $(this).attr('id');
        serv = tagid.split('_')[3];
        
    	if ($('#form_web_folder_' + serv + '_text').val() == undefined){
    		folder = "";
    	}
    	else {
    		folder = $('#form_web_folder_' + serv + '_text').val();
    	}
    	var dir = java.getFolder(folder);
    	$('#form_web_folder_' + serv + '_text').val(dir);
    });

    $(document).on('click', '.select_java', function() {
    	var serv;
    	
    	var tagid = $(this).attr('id');
        serv = tagid.split('_')[2];
        
        if ($('#form_customjvm_' + serv + '_text').val() == undefined){
    		folder = "";
    	}
    	else {
    		folder = $('#form_customjvm_' + serv + '_text').val();
    	}
    	var dir = java.getFolder(folder);
    	
    	$(' #form_customjvm_' + serv + '_text').val(dir);
    	$(' #form_radio_custom_' + serv).attr('checked', true);
		$(' #form_radio_hotspot_' + serv).attr('checked', false);
    });

    $(document).on('click', '.defaultjvm', function() {
    	$('#form_customjvm_' + selectedServer + '_text').val("");
    });
    
    function startServer( id ){
    	$( '#console_' + id ).append('<pre>Starting Server...</pre>');
		$( '#console_' + id ).append('<pre>' + java.onServerStart(id) + '</pre>');
				
		if ( java.getRunning( id ) ){			
	    	$( "body" ).find( ".data" ).addClass('hide');
	    	$( "body" ).find( "a" ).removeClass('current');
	    	$('.noapps').addClass('hide');
	    	$( 'body' ).find( '.head' ).addClass('hide');
	    	$( '#header_' + id ).removeClass('hide');
	    	
	    	$( '.settings, .plugin, #settings_footer, #console_template, #edit_' + id).addClass( 'hide' );
			$( '#console_footer, #console_' + id ).removeClass( 'hide' );
			$( '.tab' ).removeClass( 'active' );
			$('.tabs').find('a[id$="_' + selectedServer + '"]').removeClass('hide');
			$( '.j_console' ).addClass( 'active' );
			
			$( '.j_settings, .j_console, #memory_' + id + ', #lastupdate_' + id + ', #btn_save_' + id + ', #btn_delete_' + id ).removeClass( 'hide' );
			
			$( '#webapp_' + id ).addClass('running');
			$( '#btn_delete_' + id ).prop( 'disabled', true );
			$( '#btn_save_' + id ).prop( 'disabled', true );
			
			$('.start').addClass('hide');
			$('.stop, #btn_clear').removeClass('hide');
			$( '#btn_open_' + id ).prop( 'disabled', false );
		}
    }
    
    function stopServer( id ){
    	$('#console_' + id).innerHTML += '<pre>Stopping Server...</pre>';
    	$('#console_' + id).innerHTML += '<pre>' + java.onServerStop(id) + '</pre>';
    	$('.noapps').addClass('hide');
		$( '#btn_delete_' + id ).prop( 'disabled', false );
		$( '#btn_save_' + id ).prop( 'disabled', false );

    	$('#webapp_' + id).removeClass('running');
				
		$('.start').removeClass('hide');
		$('.stop').addClass('hide');	
		$( '#btn_open' ).prop( 'disabled', true );
    }
    
    function deleteApp(){
    	if (java.deleteWebApp(selectedServer)) {
    		//soft delete
	    	$( '#edit_' + selectedServer ).addClass('hide');
	    	$( '#console_' + selectedServer ).addClass('hide');
	    	$( '#btn_delete_' + selectedServer ).addClass('hide');
	    	$( '#btn_save_' + selectedServer ).addClass('hide');
	    	$( '#memory_' + selectedServer ).addClass('hide');
	    	$( '#lastupdate_' + selectedServer ).addClass('hide');
	    	$( '#webapp_' + selectedServer ).addClass('hide');
	    	
	    	$( '.j_settings' ).removeClass( 'active' );
	    	$( '.j_console' ).addClass( 'hide' );
	    	$( '.j_settings' ).addClass( 'hide' );
	    	$( '.tab' ).addClass( 'hide' ); 	
	    	$( '#console_template, #btn_clear' ).removeClass( 'hide' );
	    	$( '#settings_footer' ).addClass( 'hide' );
	    	
	    	$( 'body' ).find( '.head' ).addClass('hide');
	    	
	    	if (java.getAllDeleted())
	    		showNoAppsMsg();
	    		$('.noapps').removeClass('hide');
    	}
    }

    function addWebApp(){
    	//hide all consoles and settings
    	for (var i in apps){
    		var id = apps[i].SERVER_ID;
    		$( '#webapp_' + id ).removeClass('current');
    		$( '#console_' + id ).addClass('hide');
    		$( '#btn_save_' + id + ', #btn_delete_' + id + ', #edit_' + id ).addClass('hide');
    	}
    	
    	//hide tabs
    	$( '.j_settings, .j_console, .plugin_tab, .plugin' ).addClass( 'hide' ); 
		
		if (!newServerBool){ //if add new has already been pressed
			newServerBool = true;
			newServer = apps.length + 1;
	    	servers.push(newServer);
			
			$( '#console_template' ).after('<div class="console hide console_server" data-index="' + id + '" id="console_' + newServer + '"><p></p></div>');
			//load form
			var template = editTemplate.replace(/{x}/g, newServer);
			$('.settings').append(template);
			$('#edit_' + newServer).data("index", newServer);//set id of #edit_{x}
			
			//insert header values
			var header = headerTemplate.replace(/{x}/g, newServer);
			$('.header').append(header);
			
			
			var settingsFooter = settingsFooterTemplate.replace(/{x}/g, newServer);
			$('#settings_footer').append(settingsFooter);	
			$('#btn_save_' + newServer).data("index", newServer); //set id of #btn_save_{x}
			$('#btn_delete_' + newServer).data("index", newServer); //set id of #btn_delete_{x}
			
			var consoleFooter = consoleFooterTemplate.replace(/{x}/g, newServer);
			$('.console-info').append(consoleFooter);
			$('#memory_' + newServer).data("index", newServer); //set id of #memory_{x}
			$('#lastupdate_' + newServer).data("index", newServer); //set id of #lastupdate_{x}
			
			//add placeholder values here
			$('#form_name_' + newServer).attr("placeholder", "Webapp name");
			$('#form_ip_' + newServer).attr("placeholder", defaultHost);
			$('#form_port_' + newServer).attr("placeholder", "8080");
			$('#form_web_folder_' + newServer + '_text').attr("placeholder", "C:/path/to/webapp");
			$('#form_uri_' + newServer).attr("placeholder", "defaulturi");
			$('#form_jvmargs_' + newServer).attr("placeholder", "-d64 -server -Xms8m");
			$('#form_memory_' + newServer).attr("placeholder", "64");
			$('#form_customjvm_' + newServer + '_text').attr("placeholder", "C:/path/to/java");	
			
			$('#form_label_java_' + newServer).text(currentJvm);
			
			$( '#settings_footer, #btn_save_' + newServer + ', #btn_delete_' + newServer + ', .settings, #edit_' + newServer ).removeClass( 'hide' );
	    	$( '#console_footer, #console_template, #console_' + newServer ).addClass( 'hide' );
			$( '#btn_delete_' + newServer ).attr( 'disabled', true );
		}
		else {
			//show new settings and footer
			$( '#settings_footer, #btn_clear, #btn_save_' + servers.length + ', #btn_delete_' + servers.length + ', .settings, #edit_' + servers.length ).removeClass( 'hide' );
			//hide new console and footer
			$( '#console_footer, #console_template, #console_' + servers.length ).addClass( 'hide' );
			//disable delete button
			$( '#btn_delete_' + servers.length ).attr( 'disabled', true );
			
		}		
    }
    
    // initial set up of server list and tabs
    function refreshServerList(){
    	servers = [];
		for (var i in apps){		
			
			servers.push(apps[i].SERVER_ID);
			var id = apps[i].SERVER_ID;
			//var a = '<a id="webapp_' + id + '" class="app list_item" data-index="' + id + '" href="javascript:void(0)"><div class="action"><span class="play"></span></div>' + apps[i].SERVER_NAME + '</a>';
			var a =  '<a id="webapp_' + id + '" class="app list_item" data-index="' + id + '" href="javascript:void(0)"><span class="icon"></span>' + apps[i].SERVER_NAME + '<span class="action" id="action_' + id + '"></span></a>';
			$('#items').append(a);
			
			if (java.getRunning(id)){
		    	$('#webapp_' + id).addClass('running');
		    }
			if (apps[i].DELETED == "true"){
				$('#webapp_' + id).addClass('hide');
			}
		}
		orderList();
	}

    function refreshEditFormsAndConsoles(){
		//load web java consoles + settings page
    	var tempIp;
		for (var i in apps){
			
			var name = apps[i].SERVER_ID;
			//load console
			$( '#console_template' ).after('<div class="console hide console_server" data-index="' + name + '" id="console_' + name + '"><p><pre></pre></p></div>');
			//load console footer 
			var consoleFooter = consoleFooterTemplate.replace(/{x}/g, name);
			$('.console-info').append(consoleFooter);
			
			//load header
			var header = headerTemplate.replace(/{x}/g, name);
			$('.header').append(header);
			
			$('#info_name_' + name).text(apps[i].SERVER_NAME);
			if (apps[i].SERVER_IP == ""){
				tempIp = defaultHost;
			}
			else {
				tempIp = apps[i].SERVER_IP;
			}
			$('#info_host_' + name).text(tempIp + ":" + apps[i].SERVER_PORT);
			$('#info_dir_' + name).text(apps[i].WEBFOLDER);
			
			var settingsFooter = settingsFooterTemplate.replace(/{x}/g, name);
			$('#settings_footer').append(settingsFooter);
			
			//load form
			var template = editTemplate.replace(/{x}/g, name);
			
			$('.settings').append(template);
			$( '#edit_' + name ).removeClass( 'template' );
			$( '#edit_' + name ).addClass( 'hide' );

			//populate form
			$('#form_name_' + name).val(apps[i].SERVER_NAME);
			$('#form_ip_' + name).val(apps[i].SERVER_IP);
			$('#form_port_' + name).val(apps[i].SERVER_PORT);
			$('#form_web_folder_' + name + '_text').val(apps[i].WEBFOLDER);
			$('#form_uri_' + name).val(apps[i].DEFAULTURI);
			$('#form_jvmargs_' + name).val(apps[i].DEFAULTJVM);

			if (apps[i].CURRENTJVM == 1){
				$(' #form_radio_hotspot_' + name).prop('checked', true);
			}
			else{
				$(' #form_radio_custom_' + name).prop('checked', true);
				$(' #form_customjvm_' + name + '_text').val(apps[i].CUSTOMJVM);
			}

			document.getElementById('form_memory_' + name).value = apps[i].MEMORYJVM;
			$('#form_label_java_' + name).text(currentJvm);

			//hide form
			$( "#edit_" + name ).addClass('hide');
			
			if (apps[i].DELETED == "true"){
				$('#edit_' + name).addClass('hide');
				$('#btn_delete_' + name).addClass('hide');
				$('#btn_save_' + name).addClass('hide');
				$('#memory_' + name).addClass('hide');
				$('#lastupdate_' + name).addClass('hide');
				$('#console_' + name).addClass('hide');
			}
		}
		$('.delete').attr( 'disabled', false );
		$('#console_template').removeClass('hide');
	}

    function updateHtml(){
    	apps = JSON.parse(java.getServerConfigListAsJson());
    	//empty and reload html
    	$( '#items' ).empty();
    	$( '.settings' ).addClass( 'hide' );
    	  	

    	refreshServerList();
    }
    
    //text to show on start up
    function showNoAppsMsg(){
		$('#noappsInfo').text('You\'ve got no webapps');
		$('#noappsButton').text('+ add webapp');
		$('#noappsButton').removeClass('hide');
	}
	
	function showClickAppMsg(){
		$('#noappsInfo').text('Click on an app to start');
		$('#noappsButton').addClass('hide');
	}
    
    // footer information
    window.lastupdated = function (line, server) {
    	$('#lastupdate_' + server).text(line);
    }
    
    window.memoryupdated = function (line, server) {
    	$('#memory_' + server).text(line);
    }
    
    // close window dialog
    window.closewindow = function(count) {
    	$('#dialogText').html('Stop all apps (<span id="var">{y}</span>) running ?');
    	$('#dialogText').attr('id', 'dialog_close');
    	$('#var').text(count);
    	$("#dialog").dialog("open");
	}
        
    // plugin functions accessed from Java
    window.getPluginTab = function(name, friendlyName, server){
		var tab_template = '<a id="j_{f}_{x}" class="hide tab plugin_tab tab_{f}">{t}</a>';
		var tab = tab_template.replace(/{t}/g, name); //name to display
		var tab2 = tab.replace(/{f}/g, friendlyName); //name for ID
		var tab3 = tab2.replace(/{x}/g, server); //name for serverId
		$('.tabs').append(tab3);
	}
	
	window.getPluginView = function(name, html, server){
		var p = html.replace(/{t}/g, name);
		var p2 = p.replace(/{x}/g, server);
		$('#console_template').after(p2);
	}
	
	window.pushToPluginView = function(name, content, selectedServer){
		$('#plugin_' + name + '_' + selectedServer).get(0).innerHTML += '<pre>' + content.toString() + '</pre>';
	}
	
	window.addFileToHeader = function(filePath, type){
		if (type == "css")
			$('head').append('<link rel="stylesheet" href="' + filePath + '" type="text/css">')
		else
			$('head').append('<script src="' + filePath + '"></script>')
	}
	
	$(document).on('click', '.plugin_tab', function(e) {
		var selectedPlugin = $(this).attr('id').split('_')[1];		
		$( '#settings_footer, #console_template, .settings, .console, .plugin ' ).addClass( 'hide' );
		
		$( '.tab' ).removeClass( 'active' );
		$(this).addClass('active');
		
		$( '#plugin_' + selectedPlugin + '_' + selectedServer ).removeClass('hide');
		$( '#console_footer' ).removeClass( 'hide' );
		$( '#btn_clear' ).addClass( 'hide' );
				
		var needUpdate = java.checkForPluginUpdate(selectedPlugin.replace("dot", "."), selectedServer);
		if (needUpdate){
			java.findLog(selectedPlugin.replace("dot", "."), selectedServer);
		}
	});
    
	// response from dialog (close window  or delete confirmation)
    function callback(value){
    	if ($( '#dialog_delete' ).length ){
    		if (value == "yes"){
    			deleteApp();		
    		}
    		else{
    			//close dialog and do nothing
    		}
    		$('#dialog_delete').attr('id', 'dialogText');
    	}
    	else if ($('#dialog_close').length ){
    		if (value == "yes"){
	        	java.getButtonPressResponse(value);
	        }
	        else {
	        	//close dialog and do nothing
	        }
	        $('#dialog_close').attr('id', 'dialogText');
    	}
    }

    // alphabetical ordering of server list
    function orderList() {
    	
        var classname = document.getElementsByClassName('list_item');
        var divs = [];
        for (var i = 0; i < classname.length; ++i) {
            divs.push(classname[i]);
        }
        divs.sort(function(a, b) {
        	
            return a.innerHTML.toLowerCase().localeCompare(b.innerHTML.toLowerCase());
        });

        divs.forEach(function(el) {
        	$('#items').append(el);

        });
    }

    
    //form validation
    function validate(server) {
    	var valid = [];
    	
    	validateName($('#form_name_' + server).val());
    	validateIP($('#form_ip_' + server).val());
    	validatePort($('#form_port_' + server).val());
    	validateWebFolder($('#form_web_folder_' + server + '_text').val())
    	validateMemory($('#form_memory_' + server).val());
    	
		function validateName(name){
			if (name == "" || name == undefined){
				//$('#form_name_error_' + selectedServer).text('Please enter a valid name.');
				$('#form_name_' + server).css('border-color','red');
				valid.push(false);
			}
			else{
				$('#form_name_' + server).css('border','1px solid #c8c8c8');
				//$('#form_name_error_' + selectedServer).text(''); 
				valid.push(true);
			}
		}
	
		function validateIP(ip){
			if (ip == "" || ip == undefined){
				valid.push(true);
			}
			else {
				if (!/^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/.test(ip)){
					$('#form_ip_' + server).css('border-color','red');
					//$('#form_ip_error_' + server).text('Please enter a valid IP address.'); 
					valid.push(false);
				}
				else {
					$('#form_ip_' + server).css('border','1px solid #c8c8c8');
					//$('#form_ip_error_' + server).text('');
					valid.push(true);
				}
			}
		}
	
		function validatePort(port){
			if (port == "" || port == undefined){
				$('#form_port_' + server).css('border-color','red');
				//$('#form_port_error_' + server).text('Please enter a port.');
				valid.push(false);
			}
			else {
				if (!/^[0-9]+$/.test(port)){
					$('#form_port_' + server).css('border-color','red');
					//$('#form_port_error_' + server).text('Please enter a valid port.'); 
					valid.push(false);
				}
				else {
					$('#form_port_' + server).css('border','1px solid #c8c8c8');
					//$('#form_port_error_' + server).text('');
					valid.push(true);
				}
			}
		}
	
		function validateWebFolder(folder){
			if (folder == "" || folder == undefined) {
				$('#form_web_folder_' + server + "_text").css('border-color','red');
				//$('#form_webfolder_error_' + server).text('Please select a webapp.');
				valid.push(false);
			}
			else {
				$('#form_web_folder_' + server + "_text").css('border','1px solid #c8c8c8');
				//$('#form_webfolder_error_' + server).text('');
				valid.push(true);
			}
		}
		
		function validateMemory(memory){
			if (memory == "" || memory == undefined){
				valid.push(true);
			}
			else{
				if (!/^[0-9]+$/.test(memory)){
					$('#form_memory_' + server).css('border-color','red');
					//$('#form_memory_error_' + server).text('Please enter a valid number.'); 
					valid.push(false);
				}
				else{
					$('#form_memory_' + server).css('border','1px solid #c8c8c8');
					valid.push(true);
				}
			}
		}
		if (!(valid.indexOf(false) > -1)){
			return true;
		}
		else{
			return false;
		}		
    }
    
    java.initialisePlugins();
    
});
