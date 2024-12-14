deprecated


![image](https://user-images.githubusercontent.com/48808991/147350608-1c601788-86e3-4905-b115-eeebbc927510.png)

# RequestTracker
_**Inter-Team Request manager for KSP**_


### **RequestNode:**
###### _(Dev use only - please do not remove builder encapsulation)_
Every request is between two teams, and solves exactly one problem.
The request creator is the requester, and the team they want info from is the requestee.
If a team want to create a new request it is a root request, and so the source should be null.
If a team wants to solve request A from another team to them, but require further information from
another team to do so, then they can create a request with the request A as the source. This also
adds the newly made request as a branch in the source. Multiple branch requests can be made by the
requestee to solve the request.

This structure imposes some construction rules that should be maintained in all graph modifications:
    
1. If a request is not a root request, then its source requestee must be its requester
2. As an extension of 1, all branch requests must have the same requester, which is the requestee of their source.
3. Only tip requests (i.e. ones with no branches) can be removed/solved. If a non-tip request is marked as solved, its branches are first deleted, then it is marked as solved.


### **Higher Level Behaviour:**
Should email team leads when changes to request are made, or branch request are solved.
Should log all historical requests in a local file.
Should maintain a graph structure consisting of all root nodes.
This structure should be read in from a local file (hopefully made cloud based in the long term). 
The file will need to contain all nodes and how they are linked, not just roots.
