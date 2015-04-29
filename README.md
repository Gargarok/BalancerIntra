Most important thing to know : the development key is currently hidden within the RiotRequestSender. Its value has been *very savantly* encrypted, so that unready eyes are protected from it.
If you want to compile the source yourself, you shall have to put your own key in there, I'm afraid. I suggest taking a look at the "DEV_KEY" String constant, within the RiotRequestSender's constants. Alternative way is to use java to execute the binairies already generated, which would then use *my* very development key.

Then, the team-balancer main is the IntraBalance class, which can be given up to three parameters in its current state : 
  - a score-calculating mode specification (nothing for performance score, -c for Community score, and -b for a mix 
    of both)
  - a player-values input, to add some community-specific names and scores. Any name can do, but if the balancing 
    mode chosen uses the performance score as well, the names shall be League of Legends' nicknames though (for now, 
    there ain't no way around this, check future versions for a better system, thank you).
    The file format is then a list of lines composed by "PlayerName = X.X", without quotation marks (and the values
    may also be integer, no problem there). If the community score is not used in the calculation, the file will
    not be read (but nothing takes thee from giving useless arguments though, if such is your will) 
    The parameter to give is then -f file_path_relative_to_execution_folder
  - a player-names input, if the user likes better to write names in a text file rather than in a console. These
    names shall be the ones to be balanced, and therefore if the performance score is used they have to be 
    League of Legends' nicks as well. File syntax : one player name per line (cannot do easier than this)
    The parameter is then -t file_path_relative_to_execution_folder


This will be a glorious README file, some day. For now though, it is nothing but a small, frail little atom within the huge universe of README files. 
... But glory shall come. Yes, little atom, glory shall come. And one day, you shall become huge. One day, thou shall eat the universe. 
