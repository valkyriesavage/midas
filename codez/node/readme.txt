Installed node.js using MacPorts:
> sudo port install nodejs

Installed npm:
> curl http://npmjs.org/install.sh | sudo sh

Installed socket.io and express via npm:
> npm install socket.io express
Note: npm seems to sometimes install into a subdirectory node_modules/ of the current directory. This means you have to be careful which directory you're in when running npm - your home directory is probably a safe bet.

To check your installation, run "npm list".
Output on bjoern's machine:
dhcp-44-200:node bjoern$ npm list
/Users/bjoern
├─┬ express@2.5.8 
│ ├── connect@1.8.5 
│ ├── mime@1.2.4 
│ ├── mkdirp@0.3.0 
│ └── qs@0.4.2 
├── formidable@1.0.9 
└─┬ socket.io@0.8.7 
  ├── policyfile@0.0.4 
  ├── redis@0.6.7 
  └─┬ socket.io-client@0.8.7 
    ├── uglify-js@1.0.6 
    ├── websocket-client@1.0.0 
    └── xmlhttprequest@1.2.2