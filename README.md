# Intelligence
Manually implemented data structures: AVL Tree

The Intelligence Service monitors the Tattaglia family which has an organization structure similar to an AVL tree. The program takes the updates in the family and analysis commands as input, then outputs the changes due to the updates or the analysis results.

Family updates and analysis commands:

1) A new member joins the family: The new member is welcomed by all of its commanding superiors (starting from the boss). The program outputs who welcomes who in that order.

2) A member leaves the family: The leaving member is (possibly) replaced by another member in order to maintain the AVL property. The program outputs who is replaced by who (or who is replaced by nobody).

3) Target analysis: Given 2 family members as input, the program outputs the lowest ranking member who is a superior to both of the given members.

4) Division analysis: The program outputs the maximum number of independent members that can be targeted where no 2 members in the target list are a direct superior or inferior of the other.

5) Rank analysis: Given a family member as input, the program outputs all the members with the same rank as that member.
