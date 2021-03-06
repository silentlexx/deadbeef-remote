/*

BSD License

Copyright (C) 2013 Henry Case <rectifier04@gmail.com>
Copyright (C) 2016 Alexey Makhno <silentlexx@gmail.com>
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

     1.    Redistributions of source code must retain the above copyright notice,
           this list of conditions and the following disclaimer.
     2.    Redistributions in binary form must reproduce the above copyright notice,
           this list of conditions and the following disclaimer in the documentation
	   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

#include <stdio.h>
#include <string.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>
#include <sys/poll.h>

#include "remote.h"
#include <deadbeef/deadbeef.h>

//#define trace(...) { fprintf(stderr, __VA_ARGS__); }
#define trace(fmt,...)
#define BUF_SIZE 5

static DB_remote_plugin_t plugin;
static DB_functions_t *deadbeef;
static uintptr_t remote_mutex;
static uintptr_t remote_cond;
static intptr_t remote_tid; // thread id?
static int remote_stopthread;
int sfd; // Socket fd

static int enable;
static int kmix;

enum {
    PARAM_LISTEN = 0,
    PARAM_PORT = 1,
    PARAM_BOX = 2,
    PARAM_COUNT
};


/*
  Listen for UDP packets for actions to perform.
  Next track, prev track, play/pause, stop, etc.
 */

static void
perform_action (char buf) {

    switch (buf) {
    case '1':
	action_play_cb (NULL, NULL);
	break;
    case '2':
	action_prev_cb (NULL, NULL);
	break;
    case '3':
	action_next_cb (NULL, NULL);
	break;
    case '4':
	action_stop_cb (NULL, NULL);
	break;
    case '5':
	action_play_pause_cb (NULL, NULL);
	break;
    case '6':
	action_play_random_cb (NULL, NULL);
	break;
    case '7':
	action_toggle_stop_after_current_cb (NULL, NULL);
	break;
    case '8':
    action_volume_up_cb (NULL, NULL);
    break;
    case '9':
    action_volume_down_cb (NULL, NULL);
    break;    
    case 'a':
    action_seek_forward_cb (NULL, NULL);
    break;
    case 'b':
    action_seek_backward_cb (NULL, NULL);
    break;
    default:
	break;
    }
    return;
}

static void
remote_listen (void) {
    // start listening for udp packets.
    struct addrinfo hints, *res;
    int status;

    memset(&hints, 0, sizeof hints); // Makes sure the struct is empty, thanks beej.

const char* hostname=0; /* wildcard */

hints.ai_family=AF_UNSPEC;
hints.ai_socktype=SOCK_DGRAM;
hints.ai_protocol=0;
hints.ai_flags=AI_PASSIVE|AI_ADDRCONFIG;
  
    deadbeef->conf_lock();


    if ((status = getaddrinfo(hostname, deadbeef->conf_get_str_fast ("remote.port","11122"), &hints, &res)) != 0) {

	   fprintf (stderr, "getaddrinfo error: %s\n", gai_strerror(status));
       return;     
    }
    deadbeef->conf_unlock();
    // Do stuff
 do{/* each of the returned IP address is tried*/
    sfd=socket(res->ai_family, res->ai_socktype, res->ai_protocol);
    if(sfd>= 0)
      break; /*success*/
  }while ((res=res->ai_next) != NULL);

    if (bind(sfd,res->ai_addr,res->ai_addrlen)==-1) {
        trace("Bind error\n");
        return;
    }
    // Done with results
    freeaddrinfo (res);
}

static void
remote_thread (void *ha) {
    struct pollfd ufds[1];
    struct sockaddr_storage peer_addr;
    socklen_t peer_addr_len;
    ssize_t nread;
    char buf[BUF_SIZE];

    ufds[0].fd = sfd;
    ufds[0].events = POLLIN;

    // recvfrom and process messages.
    for (;;) {

    	if (remote_stopthread == 1) {
    	    deadbeef->mutex_unlock (remote_mutex);
    	    return;
    	}

    	peer_addr_len = sizeof (struct sockaddr_storage);

	int f = poll (ufds, 1, 1000);
	if (f == -1) {
	    printf ("error occurred in poll()");
	} else if (f == 0) {
	    continue;
	} else {
	    nread = recvfrom (sfd, buf, BUF_SIZE, 0, (struct sockaddr *) &peer_addr, &peer_addr_len);
	}

    	if (nread == -1) {
    	    continue;
    	}

    	// Do stuff with buf?
	if (buf[0] != 0) {
	    perform_action (buf[0]);
	    // We've read buf, we can clear it now.
	    buf[0] = 0;
	}
    }

    return;
}

static int
plugin_start (void) {
    deadbeef->conf_lock();

        enable = deadbeef->conf_get_int ("remote.enable", 1);
        kmix = deadbeef->conf_get_int ("remote.kmix", 0);

    deadbeef->conf_unlock();

    if(!enable) {
        return 0;
    }

    // Start plugin (duh)
    // Setup UDP listener to do stuff when receiving special datagrams
    remote_stopthread = 0;
    remote_mutex = deadbeef->mutex_create_nonrecursive ();
    //remote_cond = deadbeef->cond_create ();
    remote_listen ();

    remote_tid = deadbeef->thread_start (remote_thread, NULL);
    //
    return 0;
}

static int
plugin_stop (void) {

    if(!enable) {
        return 0;
    }

    // Stop listener and cleanup.
    if (remote_tid) {
	remote_stopthread = 1;
	trace ("waiting for thread to finish\n");
	deadbeef->thread_join (remote_tid);
	deadbeef->mutex_free (remote_mutex);
	close (sfd);
    }
    return 0;
}

int
action_play_cb (struct DB_plugin_action_s *action, DB_playItem_t *it) {
    deadbeef->sendmessage (DB_EV_PLAY_CURRENT, 0, 0, 0);
    return 0;
}

int
action_prev_cb (struct DB_plugin_action_s *action, DB_playItem_t *it) {
    deadbeef->sendmessage (DB_EV_PREV, 0, 0, 0);
    return 0;
}

int
action_next_cb (struct DB_plugin_action_s *action, DB_playItem_t *it) {
    deadbeef->sendmessage (DB_EV_NEXT, 0, 0, 0);
    return 0;
}

int
action_stop_cb (struct DB_plugin_action_s *action, DB_playItem_t *it) {
    deadbeef->sendmessage (DB_EV_STOP, 0, 0, 0);
    return 0;
}

int
action_toggle_pause_cb (struct DB_plugin_action_s *action, DB_playItem_t *it) {
    deadbeef->sendmessage (DB_EV_TOGGLE_PAUSE, 0, 0, 0);
    return 0;
}

int
action_play_pause_cb (struct DB_plugin_action_s *action, DB_playItem_t *it) {
    int state = deadbeef->get_output ()->state ();
    if (state == OUTPUT_STATE_PLAYING) {
        deadbeef->sendmessage (DB_EV_PAUSE, 0, 0, 0);
    }
    else {
        deadbeef->sendmessage (DB_EV_PLAY_CURRENT, 0, 0, 0);
    }
    return 0;
}

int
action_play_random_cb (struct DB_plugin_action_s *action, DB_playItem_t *it) {
    deadbeef->sendmessage (DB_EV_PLAY_RANDOM, 0, 0, 0);
    return 0;
}

int
action_seek_forward_cb (struct DB_plugin_action_s *action, DB_playItem_t *it) {
    deadbeef->playback_set_pos (deadbeef->playback_get_pos () + 5);
    return 0;
}

int
action_seek_backward_cb (struct DB_plugin_action_s *action, DB_playItem_t *it) {
    deadbeef->playback_set_pos (deadbeef->playback_get_pos () - 5);
    return 0;
}

int
action_volume_up_cb (struct DB_plugin_action_s *action, DB_playItem_t *it) {
    if(kmix){        
        system("qdbus org.kde.kmix /kmix/KMixWindow/actions/increase_volume org.qtproject.Qt.QAction.trigger");
    } else {
        deadbeef->volume_set_db (deadbeef->volume_get_db () + 2);
    }
    return 0;
}

int
action_volume_down_cb (struct DB_plugin_action_s *action, DB_playItem_t *it) {
    if(kmix){
        system("qdbus org.kde.kmix /kmix/KMixWindow/actions/decrease_volume org.qtproject.Qt.QAction.trigger");
    } else {
        deadbeef->volume_set_db (deadbeef->volume_get_db () - 2);
    }
    return 0;
}

int
action_toggle_stop_after_current_cb (struct DB_plugin_action_s *action, DB_playItem_t *it) {
    int var = deadbeef->conf_get_int ("playlist.stop_after_current", 0);
    var = 1 - var;
    deadbeef->conf_set_int ("playlist.stop_after_current", var);
    deadbeef->sendmessage (DB_EV_CONFIGCHANGED, 0, 0, 0);
    return 0;
}

static const char settings_dlg[] =
    "property \"Enable remote (need restart)\" checkbox remote.enable 1;"
    "property \"Port\" entry remote.port \"11122\";\n"
    "property \"Volume change via KMix\" checkbox remote.kmix 0;"
;


// define plugin interface
static DB_remote_plugin_t plugin = {
    .misc.plugin.api_vmajor = 1,
    .misc.plugin.api_vminor = 0,
    .misc.plugin.version_major = 0,
    .misc.plugin.version_minor = 1,
    .misc.plugin.type = DB_PLUGIN_MISC,
    .misc.plugin.id = "ddb_remote",
    .misc.plugin.name = "Remote control",
    .misc.plugin.descr = "Allows one to control player remotely over LAN",
    .misc.plugin.copyright =
        "BSD License\n"
        "Copyright (C) 2013 Henry Case <rectifier04@gmail.com>\n"
        "Copyright (C) 2016 Alexey Makhno <silentlexx@gmail.com>\n"
        "\n"
        "Redistribution and use in source and binary forms, with or without modification, are permitted\n"
        "provided that the following conditions are met:\n"
        "\t1. Redistributions of source code must retain the above copyright notice, this list of \n"
        "\tconditions and the following disclaimer.\n"
        "\t2. Redistributions in binary form must reproduce the above copyright notice, this list of \n"
        "\tconditions and the following disclaimer in the documentation and/or other materials\n"
        "\tprovided with the distribution.\n"
        "\n"
        "THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND\n"
        "ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED\n"
        "WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.\n"
        "IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY DIRECT, INDIRECT,\n"
        "INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT\n"
        "LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR\n"
        "PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,\n"
        "WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)\n"
        "ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY\n"
        "OF SUCH DAMAGE.\n"
    ,
    .misc.plugin.website = "https://github.com/silentlexx/deadbeef-remote/",
    .misc.plugin.start = plugin_start,
    .misc.plugin.stop = plugin_stop,
    .misc.plugin.configdialog = settings_dlg,
};

DB_plugin_t *
ddb_remote_load (DB_functions_t *api) {
    deadbeef = api;
    return DB_PLUGIN (&plugin);
}
