# Design
## Features
- startRecording(): Clears the previous path and allows a new one to start
- stopRecording(): Stops recording the current path
- quickClosePath(): Duplicates the first node at the end of the path to quickly enclose it 
/ autocomplete the last segment. Then triggers 'stopRecording'
- isAutoClosing: When within 1m (in any axis) of the start point it auto triggers 'quickClosePath'
/ less accurate for vertically overlapping paths due to WGS84 elevation being lower tolerance 
/ than horizontal movement.
## Purpose
Measure paths and perimeters on foot via GPS positioning. 
## Method
Nodes are generated when you are moving < 0.1 m/s² for 0.5s, 
or your path deviates > 10° from the previous node.
Hypothetically this will yield both greater spatial accuracy and lower memory usage, 
than similar apps that are strictly step-counted, timer operated or inter-node distance triggered. 
The coastline sampling frequency paradox is an important consideration.
