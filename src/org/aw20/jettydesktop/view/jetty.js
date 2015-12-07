$( document ).ready(function() {
	
	var apps = JSON.parse(app.getServerConfigListAsJson());
	var currentJvm = app.getJava();	
	
	var selectedServer = 0;
	var newServer = 0;
	var servers = [];
	var editTemplate = $('.settings').clone().html();
	var consoleFooterTemplate = $('.console-info').clone().html();
	
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
		$( '.settings, #settings_footer, #edit_' + selectedServer).removeClass( 'hide' );
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
    	//this.id is "webapp_1"
    	$( '.settings' ).addClass( 'hide' );
    	var tagid = this.id;
    	selectedServer = tagid.split('_')[1];
    	$( this ).addClass('current');
    	//show settings and console tabs
    	if( $( '.j_settings' ).hasClass( 'hide' ) && $( '.j_console' ).hasClass( 'hide' ) ) {
    		$('.j_settings, .j_console').removeClass('hide');
    	}
    	
    	//remove current status, hide console and edit forms from all apps
    	for (var i in apps){
    		var id = apps[i].SERVER_ID;
    		$( '#webapp_' + id ).removeClass('current');
    		$( '#console_' + id ).addClass('hide');
    		$( '#edit_' + id ).addClass('hide');
    		$( '#memory_' + id ).addClass('hide');
    		$( '#lastupdate_' + id ).addClass('hide');
    	}

    	if (!app.getRunning(selectedServer)){
    		$( '#btn_delete' ).prop( 'disabled', false );
    		$( '#btn_open' ).prop( 'disabled', true );
    	}
    	else if (app.getRunning(selectedServer)){
    		$( '#btn_delete' ).prop( 'disabled', true );
    		$( '#btn_open' ).prop( 'disabled', false );
    	}
    	
    	//hide template
    	if ( !$('#console_template').hasClass('hide') ) {
    		$('#console_template').addClass('hide');
    	}
    	
    	if ( $( '.j_settings' ).hasClass( 'active' ) ) {
    		$( '.settings, #settings_footer, #edit_' + selectedServer).removeClass( 'hide' );
	    	$( '#console_footer, #console_' + selectedServer ).addClass( 'hide' );
    	}
    	else {
	    	$( '#settings_footer, #edit_' + selectedServer ).addClass( 'hide' );
	    	$( '#console_footer, #console_' + selectedServer + ', #memory_' + selectedServer + ', #lastupdate_' + selectedServer).removeClass( 'hide' );
    	}
		//highlight current
		$( '#webapp_' + selectedServer ).addClass('current');
    });
    
    $(document).on('click', '.addwebapp', function() {
    	$( '.delete' ).attr( 'disabled', true );
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
		//re add current class & show console
		if (!selectedServer == 0){
			$( '#webapp_' + selectedServer ).addClass('current');
			$( '#console_' + selectedServer ).removeClass('hide');
			$( '#console_footer').removeClass('hide');
			$( '#settings_footer').addClass('hide');			
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
		
    });
    
    $(document).on('click', '.play', function() {    	
    	selectedServer = $(this).closest('a').attr('id').split('_')[1];   
    	
    	$(this).closest('a').addClass('current');    	
    	
    	$('#console_' + selectedServer).append('<pre>Starting Server...</pre>');
    	
		$( '#console_template' ).addClass( 'hide' );
		$( '#console_' + selectedServer ).removeClass( 'hide' );
		$( '#j_console' ).addClass( 'active' );
		$( '#j_settings' ).removeClass( 'active' );
		$( '#console_footer' ).removeClass('hide');
		$( '#settings_footer' ).addClass('hide');
		$( '#edit_' + selectedServer ).addClass( 'hide' );
		$( '.settings' ).addClass( 'hide' );
		
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
    });

    $(document).on('click', '.stop', function() {
    	selectedServer = $(this).closest('a').attr('id').split('_')[1]; 
    	$(this).closest('a').removeClass('running');
    	document.getElementById('console_' + selectedServer).innerHTML += '<pre>Stopping Server...</pre>';
    	document.getElementById('console_' + selectedServer).innerHTML += '<pre>' + app.onServerStop(selectedServer) + '</pre>';

		$( '#btn_delete' ).prop( 'disabled', false );
		$( '#btn_open' ).prop( 'disabled', true );
		
		$(this).addClass('play');
		$(this).removeClass('stop');
    });

    $('#btn_open').click(function(){

    	$( '.current' ).each(function( index ) {
    		var tagid = $(this).attr('id');
        	selectedServer = tagid.split('_')[1];
    	});

    	var host = apps[selectedServer].SERVER_IP;
    	var uri = apps[selectedServer].DEFAULTURI;

    	app.openWebApp(host, uri);
    });

    $('#btn_clear').click(function(){
    	document.getElementById('console_' + selectedServer).innerHTML = "";
    });


    $('.delete').click(function () {
    	if (app.deleteWebApp(selectedServer)) {
	    	$( '#edit_' + selectedServer ).remove();
	    	$( '#console_' + selectedServer ).remove();
	    	
	    	updateHtml();
	    	$( '.j_settings' ).removeClass( 'active' );
	    	$( '.j_console' ).addClass( 'hide' );
	    	$( '.j_settings' ).addClass( 'hide' );
	    	$( '.console_template' ).removeClass( 'hide' );
	    	$( '#settings_footer' ).addClass( 'hide' );
    	}
    });

    $(document).on('click', '.select_server', function() {
    	var folder = $('#form_web_folder_' + selectedServer + '_text').val();
    	var dir = app.getFolder(folder);
    	$('#form_web_folder_' + selectedServer + '_text').val(dir);
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
    	newServer = apps.length + 1;
    	
    	servers.push(newServer);
    	
    	$( '.j_settings, .j_console' ).addClass( 'hide' );
		
		$( '#settings_footer, #edit_' + newServer ).removeClass( 'hide' );
    	$( '#console_footer, #console_' + newServer ).addClass( 'hide' );

    	//add hide attr to all consoles + edit forms
    	for (var i in apps){
    		var id = apps[i].SERVER_ID;
    		$( '#webapp_' + id ).removeClass('current');
    		$( '#console_' + id ).addClass('hide');
    		$( '#edit_' + id ).addClass('hide');
    	}
    	$( '#console_template' ).after('<div class="console hide console_server" id="console_' + newServer + '"><p></p></div>');
		//load form
		var template = editTemplate.replace(/{x}/g, newServer);


		$('.settings').append(template);

		$('#form_label_java_' + newServer).text(currentJvm);

		$( '.settings, #edit_' + newServer ).removeClass( 'hide' );
		$( '#console_template' ).addClass( 'hide' );
    }

    function refreshServerList(){
    	servers = [];
    	
		for (var i in apps){
			
			servers.push(apps[i].SERVER_ID);
			var id = apps[i].SERVER_ID;
			
			var a = '<a id="webapp_' + id + '" class="app list_item" href="javascript:void(0)"><div class="action" title="' + apps[i].SERVER_NAME + '"><span class="play"></span></div>' + apps[i].SERVER_NAME + '</a>';
			
			$('#items').append(a);
		}
		orderList();
	}

    function refreshEditFormsAndConsoles(){
		//load web app consoles + settings page
    	
		for (var i in apps){
			var name = apps[i].SERVER_ID;
			
			//load console
			$( '#console_template' ).after('<div class="console hide console_server" id="console_' + name + '"><p><pre></pre></p></div>');
			//load console footer 
			var consoleFooter = consoleFooterTemplate.replace(/{x}/g, name);
			$('.console-info').append(consoleFooter);
			
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
				app.outputToEclipse(apps[i].CUSTOMJVM);
				$(' #form_customjvm_' + name + '_text').val(apps[i].CUSTOMJVM);
			}

			document.getElementById('form_memory_' + name).value = apps[i].MEMORYJVM;
			$('#form_label_java_' + name).text(currentJvm);

			//hide form
			$( "#edit_" + name ).addClass('hide');
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

    //form validation - NONE OF THIS WORKS ON NEW FORM YET
    /*function validateFormOnSubmit(theForm) {

		var reason = "";
	    reason += validateName(theForm.elements["server_name"].value);
	    reason += validateIP(theForm.elements["ip_address"].value);
	    reason += validatePort(theForm.elements["port"].value);
	    reason += validateWebFolder(theForm.web_folder);

	    if (reason != "") {
	        document.getElementById("p1").innerHTML = "Some fields need correction:\n" + reason;
	    } else {
	        simpleCart.checkout();
	    }
	    return false;
	}

	function validateName(name){
		if (name == "")
			return "Please enter a name. ";
		else
			return "";
	}

	function validateIP(ip){
		if (ip == "")
			return "Please enter an ip address. ";
		else
			return "";
	}

	function validatePort(port){
		if (port == "")
			return "Please enter a port. ";
		else
			return "";
	}

	function validateWebFolder(folder){
		if (folderme == "")
			return "Please enter a folder. ";
		else
			return "";
	}*/
});
