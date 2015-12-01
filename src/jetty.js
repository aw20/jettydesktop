$( document ).ready(function() {

	var apps = JSON.parse(app.getServerConfigListAsJson());
	var currentJvm = app.getJava();
	var jsonapps = app.getServerConfigListAsJson();
	var selectedServer = "0";
	var servers = [];
	var editTemplate = $('.editoptions').clone().html();
	
	$( '#console_template' ).addClass("hide");
	
	//get and list saved webapps
	refreshServerList();
	refreshEditFormsAndConsoles();
	
	$(document).on('click', '.j_edit', function() {
		if( $( '.editoptions' ).hasClass( 'hide' ) ) {
			$( '.editoptions' ).removeClass( 'hide' );
			$( '#console_' + selectedServer ).addClass( 'hide' );
		} else {
			$( '.editoptions' ).addClass( 'hide' );
			$( '#console_' + selectedServer ).removeClass( 'hide' );
		}
		
		//get current webapp from list
    	$( ".current" ).each(function( index ) {
    		var tagid = $(this).attr('id');
        	selectedServer = tagid.split('_')[1];
    	});    	
    	
    	//display current edit window
    	$( '#edit_' + selectedServer ).removeClass("hide");
	});
    
    $(document).on('click', '.app', function() {
    	//this.id is "webapp_1"
    	$( '.editoptions' ).addClass( 'hide' );
    	var tagid = this.id;
    	selectedServer = tagid.split('_')[1];
    	$( this ).addClass("current");
    	
    	//remove current status, hide console and edit forms from all apps
    	for (var i in apps){
    		var id = apps[i].SERVER_ID;
    		$( "#webapp_" + id ).removeClass("current");
    		$( '#console_' + id ).addClass("hide");
    		$( '#edit_' + id ).addClass("hide");
    	}    	
    	
    	if (!app.getRunning(selectedServer)){
    		//disable edit, delete, start buttons
    		$( "#btn_edit" ).prop( "disabled", false );
    		$( "#btn_delete" ).prop( "disabled", false );
    		$( "#btn_start" ).prop( "disabled", false );
    		//enable open button
    		$( "#btn_open" ).prop( "disabled", true );    		
    		$( "#btn_stop" ).prop( "disabled", true );
    	}
    	else if (app.getRunning(selectedServer)){
    		//disable edit, delete, start buttons
    		$( "#btn_edit" ).prop( "disabled", true );
    		$( "#btn_delete" ).prop( "disabled", true );
    		$( "#btn_start" ).prop( "disabled", true );
    		//enable open button
    		$( "#btn_open" ).prop( "disabled", false );    		
    		$( "#btn_stop" ).prop( "disabled", false );
    	}
    	
    	//show console
    	$('#console_template').addClass("hide");
    	$( '#console_' + selectedServer ).removeClass("hide");
		$( '#edit_' + selectedServer ).addClass("hide");
		//highlight current
		$( "#webapp_" + selectedServer ).addClass("current");
    });
    
    $(document).on('click', '.select_server', function() {
    	//get last element in servers array
    	var dir = app.getFolder();
    	$('#form_web_folder_' + selectedServer + '_text').val(dir);
    });
    
    $(document).on('click', '#btn_add', function() {
    	selectedServer = apps.length + 1;
    	servers.push(selectedServer);
    	
    	//add hide attr to all consoles + edit forms
    	for (var i in apps){
    		var id = apps[i].SERVER_ID;
    		$( "#webapp_" + id ).removeClass("current");
    		$( '#console_' + id ).addClass("hide");
    		$( '#edit_' + id ).addClass("hide");
    	}
    	$( "#console_template" ).after('<div class="console hide console_server" id="console_' + selectedServer + '"></div>');
		//load form
		var template = editTemplate.replace(/{x}/g, selectedServer);
		
		$('.editoptions').append(template);
		$( '#edit_' + selectedServer ).removeClass( 'hide' );
		$( '.editoptions' ).removeClass( 'hide' );
		$( '#console_template' ).addClass( 'hide' );
    });
    
    $(document).on('click', '.j_save', function() {
    	
    	$( ".current" ).each(function( index ) {
    		var tagid = $(this).attr('id');
        	selectedServer = tagid.split('_')[1];
    	});    	
    	
    	var name = $('#form_name_' + selectedServer).val();
    	var ip = $('#form_ip_' + selectedServer).val();
		var port = $('#form_port_' + selectedServer).val();
		var webFolder = $('#form_web_folder_' + selectedServer + "_text").val();
		var uri = $('#form_uri_' + selectedServer).val();
		var defaultJvm = $(' #form_radio_custom_' + selectedServer).is(':checked');
		var customJvmBool = $(' #form_radio_custom_' + selectedServer).is(':checked');
		var customJvm = $(' #form_customjvm_' + selectedServer).val();
		var jvmArgs = $(' #form_jvmargs_' + selectedServer).val();
		var memory = $(' #form_memory_' + selectedServer).val();
		
		//new app
		if (apps.length != servers.length){
			app.saveSettings(true, selectedServer, name, ip, port, webFolder, uri, defaultJvm, customJvmBool, customJvm, jvmArgs, memory);
		}
		//existing app
		else {
			app.saveSettings(false, selectedServer, name, ip, port, webFolder, uri, defaultJvm, customJvmBool, customJvm, jvmArgs, memory);
		}    	
		
		updateHtml();
		//re add current class & show console
		$( "#webapp_" + selectedServer ).addClass("current");
		$( "#console_" + selectedServer ).removeClass("hide");
		//disable edit, delete, start buttons
		$( "#btn_edit" ).prop( "disabled", false );
		$( "#btn_delete" ).prop( "disabled", false );
		$( "#btn_start" ).prop( "disabled", false );
		//enable open button
		$( "#btn_open" ).prop( "disabled", true );
		$( "#btn_clear" ).prop( "disabled", false );
		$( "#btn_stop" ).prop( "disabled", true );
    });
   
    
    $(document).on('click', '.j_cancel', function() {   
    	
    	if( $( '.editoptions' ).hasClass( 'hide' ) ) {
			$( '.editoptions' ).removeClass( 'hide' );
		} else {
			$( '.editoptions' ).addClass( 'hide' );
		}
    	
    	//get current webapp from list
    	$( ".current" ).each(function( index ) {
    		var tagid = $(this).attr('id');
        	selectedServer = tagid.split('_')[1];
    	}); 
    	
    	$( '#edit_' + selectedServer ).addClass("hide");
    	$( '#console_' + selectedServer ).removeClass("hide");
    	
    });
    
    $('#btn_stop').click(function (){
    	document.getElementById('console_' + selectedServer).innerHTML += '<pre>Stopping Server...</pre>';
    	document.getElementById('console_' + selectedServer).innerHTML += '<pre>' + app.onServerStop(selectedServer) + '</pre>';   	
    	
    	//enable edit, delete, start buttons
		$( "#btn_edit" ).prop( "disabled", false );
		$( "#btn_delete" ).prop( "disabled", false );
		$( "#btn_start" ).prop( "disabled", false );
		//disable open, stop buttons
		$( "#btn_open" ).prop( "disabled", true );
		$( "#btn_stop" ).prop( "disabled", true );
    });
    
    $('#btn_open').click(function(){    	
    	
    	$( ".current" ).each(function( index ) {
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
    
    
    $('#btn_delete').click(function () {
    	app.deleteWebApp(selectedServer);
    	//only to be called if actually deleted - check on Java side.
    	updateHtml();
    });
    
    $('#btn_start').click(function (){
    	document.getElementById('console_' + selectedServer).innerHTML += '<pre>Starting Server...</pre>';
    	//var running = true;
    	
		$( '#console_template' ).addClass( 'hide' );
		$( '#console_' + selectedServer ).removeClass( 'hide' );
		$( '#edit_' + selectedServer ).addClass( 'hide' );
		$( '.editoptions' ).addClass( 'hide' );
		
		//call after rest is complete
		document.getElementById('console_' + selectedServer).innerHTML += '<pre>' + app.onServerStart(selectedServer) + '</pre>';
				
		$('#console_' + selectedServer).removeClass("hide");
		
		if (app.getRunning(selectedServer)){
			//disable edit, delete, start buttons
			$( "#btn_edit" ).prop( "disabled", true );
			$( "#btn_delete" ).prop( "disabled", true );
			$( "#btn_start" ).prop( "disabled", true );
			//enable open button
			$( "#btn_open" ).prop( "disabled", false );
			$( "#btn_stop" ).prop( "disabled", false );
		}
		else {
			//disable edit, delete, start buttons
			$( "#btn_edit" ).prop( "disabled", false );
			$( "#btn_delete" ).prop( "disabled", false );
			$( "#btn_start" ).prop( "disabled", false );
			//enable open button
			$( "#btn_open" ).prop( "disabled", true );
			$( "#btn_stop" ).prop( "disabled", true );
		}		
    });

    
    function refreshServerList(){
    	servers = [];
		for (var i in apps){
			servers.push(apps[i].SERVER_ID);
			var id = apps[i].SERVER_ID;
			
			var a = '<a id="webapp_' + id + '" class="app list_item" data-index="' + id + '" href="javascript:void(0)">' + apps[i].SERVER_NAME + '</a>';
	
		    $('#items').append(a);
		}
		orderList();
	}
    
    function refreshEditFormsAndConsoles(){
		//load web app consoles + settings page
		for (var i in apps){			
			var name = apps[i].SERVER_ID;			
			//load console
			$( "#console_template" ).after('<div class="console hide console_server" id="console_' + name + '"></div>');	
			//load form
			var template = editTemplate.replace(/{x}/g, name);	
			$('.editoptions').append(template);
			$( '#edit_' + name ).removeClass( 'template' );			
			
			//populate form
			$('#form_name_' + name).val(apps[i].SERVER_NAME);
			$('#form_ip_' + name).val(apps[i].SERVER_IP);
			$('#form_port_' + name).val(apps[i].SERVER_PORT);
			$('#form_web_folder_' + name + '_text').val(apps[i].WEBFOLDER);
			$('#form_uri_' + name).val(apps[i].DEFAULTURI);
			$('#form_jvmargs_' + name).val(apps[i].DEFAULTJVM);
			if (apps[i].CURRENTJVM == 1){
				document.getElementById('form_radio_hotspot_' + name).checked = true;
			}
			else{
				document.getElementById('form_radio_hotspot_' + name).checked = false;
			}
			document.getElementById('form_memory_' + name).value = apps[i].MEMORYJVM;
			$('#form_label_java_' + name).text(currentJvm);
			
			//hide form
			$( "#edit_" + name ).addClass("hide");			
		}
		$('#console_template').removeClass("hide");
	}
    
    function updateHtml(){
    	apps = JSON.parse(app.getServerConfigListAsJson());
    	//empty and reload html
    	$( "#items" ).empty();
    	$( '.editoptions' ).addClass( 'hide' );
    	
    	refreshServerList();
    	//app.outputToEclipse(document.documentElement.innerHTML);
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
    function validateFormOnSubmit(theForm) {

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
	}	
});