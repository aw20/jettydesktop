$( document ).ready(function() {
	
	var apps = JSON.parse(app.getServerConfigListAsJson());
	var currentJvm = app.getJava();	
	var newServerBool = false;
	
	var selectedServer = 0;
	var newServer = 0;
	var servers = [];
	var editTemplate = $('.settings').clone().html();
	var consoleFooterTemplate = $('.console-info').clone().html();
	var settingsFooterTemplate = $('#settings_footer').clone().html();
	
	//get and list saved webapps
	refreshServerList();
	refreshEditFormsAndConsoles();
    
	
	$( '.j_reveal' ).on( 'click', function() {
        $( '.main' ).toggleClass( 'reveal' );
    });
	
	if (apps == "") {	
		addWebApp();
	}
	
	$(document).on('click', '.j_settings', function() {
		$( '#settings_footer, #btn_save_' + selectedServer + ', #btn_delete_' + selectedServer + ', #edit_' + selectedServer + ', .settings' ).removeClass( 'hide' );
    	$( '#console_footer, #console_' + selectedServer ).addClass( 'hide' );
		
		$( '.j_settings' ).addClass( 'active' );
		$( '.j_console' ).removeClass( 'active' );

		//get current webapp from list
    	$( '.current' ).each(function( index ) {
    		var tagid = $(this).attr('id');
        	selectedServer = tagid.split('_')[1];
    	});

    	//display current edit window
    	$( '#edit_' + selectedServer ).removeClass('hide');
	});
	
	$(document).on('click', '.j_console', function() {
		$( '.settings, #settings_footer, #edit_' + selectedServer).addClass( 'hide' );
		$( '#console_footer, #console_' + selectedServer ).removeClass( 'hide' );
		$( '.j_settings' ).removeClass( 'active' );
		$( '.j_console' ).addClass( 'active' );
		//get current webapp from list
		$( '.current' ).each(function( index ) {
			var tagid = $(this).attr('id');
	    	selectedServer = tagid.split('_')[1];
		});
	
		//display current edit window
		$( '#edit_' + selectedServer ).removeClass('hide');
	});

    $(document).on('click', '.app', function() {
    	
    	$( "body" ).find( ".data" ).addClass('hide');
    	
    	//this.id is "webapp_1"
    	$( '.settings' ).addClass( 'hide' );
    	var tagid = this.id;
    	selectedServer = $(this).data('index');
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

    	if (!app.getRunning(selectedServer)){
    		$( '#btn_delete_' + id ).prop( 'disabled', false );
    		$( '#btn_open' ).prop( 'disabled', true );
    	}
    	else if (app.getRunning(selectedServer)){
    		$( '#btn_delete_' + id ).prop( 'disabled', true );
    		$( '#btn_open' ).prop( 'disabled', false );
    	}
    	
    	//hide template
    	if ( !$('#console_template').hasClass('hide') ) {
    		$('#console_template').addClass('hide');
    	}
    	
    	if ( $( '.j_settings' ).hasClass( 'active' ) ) {
    		$( '.settings, #btn_save_' + selectedServer + ', #btn_delete_' + selectedServer + ', #edit_' + selectedServer).removeClass( 'hide' );
	    	$( '#console_footer, #console_' + selectedServer ).addClass( 'hide' );
    	}
    	else {
    		$( '.j_console' ).addClass( 'active' );
	    	$( '#settings_footer, #edit_' + selectedServer ).addClass( 'hide' );
	    	$( '#console_footer, #console_' + selectedServer + ', #memory_' + selectedServer + ', #lastupdate_' + selectedServer).removeClass( 'hide' );
    	}
		//highlight current
		$( '#webapp_' + selectedServer ).addClass('current');
		
    });
    
    $(document).on('click', '.addwebapp', function() {    	
    	addWebApp();
    });

    $(document).on('click', '.j_save', function() {
    	var savedServer = selectedServer;
    	
    	if (newServer != 0){
    		savedServer = newServer;
    	}	

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

		//new app
		if (apps.length != servers.length){
			app.saveSettings(true, savedServer, name, ip, port, webFolder, uri, defaultJvm, customJvmBool, customJvm, jvmArgs, memory);
		}
		//existing app
		else {
			app.saveSettings(false, savedServer, name, ip, port, webFolder, uri, defaultJvm, customJvmBool, customJvm, jvmArgs, memory);
		}

		updateHtml();
		
		newServerBool = false;
		$( '.delete' ).attr( 'disabled', false );
		//re add current class & show console
		if (!selectedServer == 0){
			$( '#webapp_' + selectedServer ).addClass('current');
			$( '#console_' + selectedServer ).removeClass('hide');
			$( '#console_footer').removeClass('hide');
			$( '#settings_footer').addClass('hide');
			$( '.j_console, .j_settings' ).removeClass('hide');
			$( '.j_console' ).addClass( 'active' );
		}
		else {
			$( '#console_template' ).removeClass('hide');
		}
		//disable edit, delete, start buttons
		$( '#btn_delete' ).prop( 'disabled', false );
		//enable open button
		$( '.delete' ).attr( 'disabled', false );
		$( '#btn_clear' ).prop( 'disabled', false );
		
		$( '#settings_footer').addClass('hide');
		$( '#console_template' ).addClass( 'hide' );
		orderList();
    });
    
    $(document).on('click', '#get_html', function() { 
    	app.outputToEclipse(document.documentElement.innerHTML);
    });
    
    $(document).on('click', '.play', function(e) {    	
    	selectedServer = $(this).closest('a').attr('id').split('_')[1];
    	$( "body" ).find( ".data" ).addClass('hide');
    	$( "body" ).find( "a" ).removeClass('current');
    	$( this ).closest('a').addClass('current');
    	
    	$( '.settings, #settings_footer, #console_template, #edit_' + selectedServer).addClass( 'hide' );
		$( '#console_footer, #console_' + selectedServer ).removeClass( 'hide' );
		$( '.j_settings' ).removeClass( 'active' );
		$( '.j_console' ).addClass( 'active' );
    	
    	$(this).closest('a').addClass('current');    	
    	
    	$('#console_' + selectedServer).append('<pre>Starting Server...</pre>');
    	
		$( '.j_settings, .j_console, #memory_' + selectedServer + ', #lastupdate_' + selectedServer ).removeClass( 'hide' );
		
		$('#console_' + selectedServer).append('<pre>' + app.onServerStart(selectedServer) + '</pre>');
				
		if (app.getRunning(selectedServer)){
			$(this).closest('a').addClass('running');
			$( '#btn_delete' ).prop( 'disabled', true );
			$( '#btn_open' ).prop( 'disabled', false );
			$(this).removeClass('play');
			$(this).addClass('stop');
		}
		else {
			$( '#btn_delete' ).prop( 'disabled', false );
			$( '#btn_open' ).prop( 'disabled', true );
		}
	
		e.stopPropagation();
    });

    $(document).on('click', '.stop', function(e) {
    	selectedServer = $(this).closest('a').attr('id').split('_')[1]; 
    	$(this).closest('a').removeClass('running');
    	document.getElementById('console_' + selectedServer).innerHTML += '<pre>Stopping Server...</pre>';
    	document.getElementById('console_' + selectedServer).innerHTML += '<pre>' + app.onServerStop(selectedServer) + '</pre>';

		$( '#btn_delete' ).prop( 'disabled', false );
		$( '#btn_open' ).prop( 'disabled', true );
		
		$(this).addClass('play');
		$(this).removeClass('stop');
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
    	    	
    	    	app.openWebApp(host, uri);
    		}
    	}
    	
    });

    $(document).on('click', '#btn_clear', function() {
    	document.getElementById('console_' + selectedServer).innerHTML = "";
    });

    $(document).on('click', '.delete', function() {
    	if (app.deleteWebApp(selectedServer)) {
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
	    	$( '#console_template' ).removeClass( 'hide' );
	    	$( '#settings_footer' ).addClass( 'hide' );
    	}
    });

    $(document).on('click', '.select_server', function() {
    	var folder;
    	var serv;
    	
    	$( '.select_server' ).each(function( index ) {
    		var tagid = $(this).attr('id');
    		if (!$(this).closest('.edit').hasClass('hide')){
    			var tagid = $(this).closest('.edit').attr('id');
            	serv = tagid.split('_')[1];
    		}
    	});

    	if ($('#form_web_folder_' + serv + '_text').val() == undefined){
    		folder = "";
    	}
    	else {
    		folder = $('#form_web_folder_' + serv + '_text').val();
    	}
    	var dir = app.getFolder(folder);
    	$('#form_web_folder_' + serv + '_text').val(dir);
    });

    $(document).on('click', '.select_java', function() {
    	var java = app.getFolder();
    	$('#form_customjvm_' + selectedServer + '_text').val(java);
    	$(' #form_radio_custom_' + selectedServer).attr('checked', true);
		$(' #form_radio_hotspot_' + selectedServer).attr('checked', false);
    });

    $('.defaultjvm').click(function () {
    	$('#form_customjvm_' + selectedServer + '_text').val("");
    });
    
    function addWebApp(){
    	//hide all consoles and settings
    	for (var i in apps){
    		var id = apps[i].SERVER_ID;
    		$( '#webapp_' + id ).removeClass('current');
    		$( '#console_' + id ).addClass('hide');
    		$( '#btn_save_' + id + ', #btn_delete_' + id + ', #edit_' + id ).addClass('hide');
    	}
    	
    	//hide tabs
    	$( '.j_settings, .j_console' ).addClass( 'hide' ); 
		
		if (!newServerBool){ //if add new has already been pressed
			newServerBool = true;
			newServer = apps.length + 1;
	    	servers.push(newServer);
			
			$( '#console_template' ).after('<div class="console hide console_server" data-index="' + id + '" id="console_' + newServer + '"><p></p></div>');
			//load form
			var template = editTemplate.replace(/{x}/g, newServer);
			$('.settings').append(template);
			$('#edit_' + newServer).data("index", newServer);//set id of #edit_{x}
			
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
			$('#form_ip_' + newServer).attr("placeholder", "127.0.0.1");
			$('#form_port_' + newServer).attr("placeholder", "8080");
			$('#form_web_folder_' + newServer + '_text').attr("placeholder", "C:/path/to/webapp");
			$('#form_uri_' + newServer).attr("placeholder", "defaulturi");
			$('#form_jvmargs_' + newServer).attr("placeholder", "-server -Xms2G -Xmx2G");
			$('#form_memory_' + newServer).attr("placeholder", "64");
			$('#form_customjvm_' + newServer + '_text').attr("placeholder", "C:/path/to/java");	
			
			$('#form_label_java_' + newServer).text(currentJvm);
			
			$( '#settings_footer, #btn_save_' + newServer + ', #btn_delete_' + newServer + ', .settings, #edit_' + newServer ).removeClass( 'hide' );
	    	$( '#console_footer, #console_template, #console_' + newServer ).addClass( 'hide' );
			$( '#btn_delete_' + newServer ).attr( 'disabled', true );
		}
		else {
			//show new settings and footer
			$( '#btn_save_' + servers.length + ', #btn_delete_' + servers.length + ', .settings, #edit_' + servers.length ).removeClass( 'hide' );
			//hide new console and footer
			$( '#console_footer, #console_template, #console_' + servers.length ).addClass( 'hide' );
			//disable delete button
			$( '#btn_delete_' + servers.length ).attr( 'disabled', true );
			
		}
		
    }

    function refreshServerList(){
    	servers = [];
		for (var i in apps){		
			
			servers.push(apps[i].SERVER_ID);
			var id = apps[i].SERVER_ID;
			var a = '<a id="webapp_' + id + '" class="app list_item" data-index="' + id + '" href="javascript:void(0)"><div class="action" title="' + apps[i].SERVER_NAME + '"><span class="play"></span></div>' + apps[i].SERVER_NAME + '</a>';
			
			$('#items').append(a);
			
			if (app.getRunning(id)){
		    	$('#webapp_' + id).find('span').addClass('running').addClass('stop').removeClass('play');
		    }
			if (apps[i].DELETED == "true"){
				$('#webapp_' + id).addClass('hide');
			}
		}
		orderList();
		
	}

    function refreshEditFormsAndConsoles(){
		//load web app consoles + settings page
    	
		for (var i in apps){
			var name = apps[i].SERVER_ID;
			
			//load console
			$( '#console_template' ).after('<div class="console hide console_server" data-index="' + name + '" id="console_' + name + '"><p><pre></pre></p></div>');
			//load console footer 
			var consoleFooter = consoleFooterTemplate.replace(/{x}/g, name);
			$('.console-info').append(consoleFooter);
			
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
		$('#console_template').removeClass('hide');
	}

    function updateHtml(){
    	apps = JSON.parse(app.getServerConfigListAsJson());
    	//empty and reload html
    	$( '#items' ).empty();
    	$( '.settings' ).addClass( 'hide' );
    	  	

    	refreshServerList();
    }
    
    window.lastupdated = function (line, server) {
    	$('#lastupdate_' + server).text(line);
    }
    
    window.memoryupdated = function (line, server) {
    	$('#memory_' + server).text(line);
    }

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
});
