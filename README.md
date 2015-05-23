# comp90015.proj2
Project 2 (a Master/Worker job system) for Distributed Systems at The University of Melbourne.

# First Time Setup

1. Clone the repository `ssh://git@github.com/nedp/comp90015.proj2`.
2. Change to branch `master`
3. Install maven from https://maven.apache.org/download.cgi --
   Follow Maven's README instructions.
4. Run `mvn test` to check that all technologies are working on your machine.


## git
Workflow:

1. Checkout master
2. Create a new branch to work in
3. Do work in the new branch
4. Re-pull into the new branch from master to resolve merge conflicts
5. Pull request the new branch back into master and wait for review/approval from someone else

Branch names should identify the feature/component being worked on and who is responsible for it; eg. `nedp/job-starting`

Remember to add any user-specific project settings files (eg. for IDEs) to .gitignore.
