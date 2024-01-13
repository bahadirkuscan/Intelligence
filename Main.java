import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static Member boss;  // store the boss separately

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java Main <input_file> <output_file>");
            System.exit(1);
        }

        String inputFilePath = args[0];
        String outputFilePath = args[1];

        try {
            Scanner scanner = new Scanner(new File(inputFilePath));
            boss = new Member(scanner.next(), Double.parseDouble(scanner.next()), null, 0);

            // Create a PrintWriter to write output to the output file
            PrintWriter outputWriter = new PrintWriter(new File(outputFilePath));

            while (scanner.hasNext()) {
                String command = scanner.next();
                switch (command) {
                    case "MEMBER_IN":
                        Member added_member = add_member(scanner.next(), Double.parseDouble(scanner.next()), boss, boss);
                        ArrayList<String> greeting_members = new ArrayList<>();
                        Member greeting_member = added_member.superior;
                        while (greeting_member != null){
                            height_updater(greeting_member);   // height of all the superiors of the added member might change therefore they better be updated
                            greeting_members.add(0, greeting_member.name);
                            greeting_member = greeting_member.superior;
                        }
                        for (String greeter : greeting_members){
                            outputWriter.write(greeter + " welcomed " + added_member.name + "\n");
                        }
                        Member reorganizer = added_member.superior;    // reorganization process begins with immediate superior of the new member
                        while (reorganizer != null){
                            reorganize(reorganizer);
                            reorganizer = reorganizer.superior;
                        }
                        break;

                    case "MEMBER_OUT":
                        String leaving_name = scanner.next();
                        Member leaving_member = find_member(Double.parseDouble(scanner.next()), boss);
                        Member reorganizing_member = leaving_member.superior;   // immediate superior of the leaving member starts reorganization if there are no replacements
                        Member[] replacing_and_former = remove_member(leaving_member);
                        Member height_update = replacing_and_former[1];     // heights should be updated starting from replacing member's superior all the way to the boss
                        while (height_update != null){
                            height_updater(height_update);
                            height_update = height_update.superior;
                        }
                        if(replacing_and_former[0] != null){    // there is a replacer
                            reorganizing_member = replacing_and_former[1];
                        }
                        while (reorganizing_member != null){    //reorganization process
                            reorganize(reorganizing_member);
                            reorganizing_member = reorganizing_member.superior;
                        }

                        if (replacing_and_former[0] == null){
                            outputWriter.write(leaving_name + " left the family, replaced by nobody" + "\n");
                        }
                        else {
                            outputWriter.write(leaving_name + " left the family, replaced by " + replacing_and_former[0].name + "\n");
                        }
                        break;

                    case "INTEL_TARGET":
                        scanner.next();
                        double target1_gms = Double.parseDouble(scanner.next());
                        scanner.next();
                        double target2_gms = Double.parseDouble(scanner.next());
                        Member search_member = boss;        // searching the member using gms, result is the first member whose gms falls between the two targets for the first time
                        while (!(search_member.gms > Math.min(target1_gms, target2_gms) && search_member.gms < Math.max(target1_gms,target2_gms))){
                            if (search_member.gms > Math.max(target1_gms, target2_gms)){
                                search_member = search_member.lower_gms_inferior;
                            }
                            else if (search_member.gms < Math.min(target1_gms, target2_gms)){
                                search_member = search_member.higher_gms_inferior;
                            }
                            else{  // one of the targets is a superior of the other, gms can't fall in between
                                break;
                            }
                        }
                        outputWriter.write("Target Analysis Result: " + search_member.name + " " + String.format("%.3f",search_member.gms) + "\n");
                        break;

                    case "INTEL_DIVIDE":
                        Member.division_analysis_result = 0;    // reset the previous analysis result
                        division_analysis(boss);
                        outputWriter.write("Division Analysis Result: " + Member.division_analysis_result + "\n");
                        break;

                    case "INTEL_RANK":
                        scanner.next();
                        double search_gms = Double.parseDouble(scanner.next());
                        int search_rank = 0;
                        Member member_search = boss;
                        while (member_search.gms != search_gms){    // find the requested rank
                            if(member_search.gms > search_gms){
                                member_search = member_search.lower_gms_inferior;
                            }
                            else {
                                member_search = member_search.higher_gms_inferior;
                            }
                            search_rank += 1;
                        }
                        outputWriter.write("Rank Analysis Result:");
                        for (Member member : rank_search(search_rank, boss, new ArrayList<>())){
                            outputWriter.write(" " + member.name + " " + String.format("%.3f",member.gms));
                        }
                        outputWriter.write("\n");
                        break;
                }
            }

            // Close the output file
            outputWriter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Member add_member(String name, double gms, Member past_member, Member current_member) {     // returns the inserted member
        while (current_member != null) {    // locate the spot
            if (gms < current_member.gms) {
                past_member = current_member;
                current_member = current_member.lower_gms_inferior;
            }
            else if (gms > current_member.gms) {
                past_member = current_member;
                current_member = current_member.higher_gms_inferior;
            }
        }
        current_member = new Member(name,gms,past_member,0); // create the member with its superior assigned
        if (past_member.gms < current_member.gms){      // assign past member's inferior as the new member
            past_member.higher_gms_inferior = current_member;
        }
        else if (past_member.gms > current_member.gms){
            past_member.lower_gms_inferior = current_member;
        }
        return current_member;
    }


    // return the replacing member and its former superior
    public static Member[] remove_member (Member member){
        Member replacing_member;
        Member former_superior;

        if (member.lower_gms_inferior == null && member.higher_gms_inferior == null){   // no inferiors
            try {
                if (member.superior.gms < member.gms){
                    member.superior.higher_gms_inferior = null;
                }
                else{
                    member.superior.lower_gms_inferior = null;
                }
            }
            catch (NullPointerException e){ // boss is removed
                boss = null;
            }
            former_superior = member.superior;
            return new Member[]{null, former_superior};
        }

        else if (member.lower_gms_inferior != null && member.higher_gms_inferior != null){    // has 2 inferiors
            replacing_member = member.higher_gms_inferior;
            while (replacing_member.lower_gms_inferior != null){        // find replacer
                replacing_member = replacing_member.lower_gms_inferior;
            }

            if (replacing_member.superior.gms == member.gms){   // special case: replacing member is the immediate inferior
                member.name = replacing_member.name;
                member.gms = replacing_member.gms;
                member.higher_gms_inferior = replacing_member.higher_gms_inferior;
                try{
                    replacing_member.higher_gms_inferior.superior = member;
                }
                catch (NullPointerException e){}
                former_superior = member;
            }
            else{
                if (replacing_member.higher_gms_inferior != null){
                    replacing_member.higher_gms_inferior.superior = replacing_member.superior;  // reconnecting replacing member's inferior
                    replacing_member.superior.lower_gms_inferior = replacing_member.higher_gms_inferior;
                }
                else{
                    replacing_member.superior.lower_gms_inferior = null;    // replacing member is removed from its original spot
                }
                former_superior = replacing_member.superior;

                //replacement
                member.name = replacing_member.name;
                member.gms = replacing_member.gms;
            }

            return new Member[]{member,former_superior};
        }

        else{    // has 1 inferior
            if (member.lower_gms_inferior != null) {
                replacing_member = member.lower_gms_inferior;
                member.name = replacing_member.name;
                member.gms = replacing_member.gms;
                member.lower_gms_inferior = null;
            }
            else{
                replacing_member = member.higher_gms_inferior;
                member.name = replacing_member.name;
                member.gms = replacing_member.gms;
                member.higher_gms_inferior = null;
            }
            former_superior = member;
            return new Member[]{replacing_member,former_superior};
        }
    }

    public static Member find_member (double gms, Member current_member){
        while (current_member.gms != gms){
            if (current_member.gms < gms){
                current_member = current_member.higher_gms_inferior;
            }
            else {
                current_member = current_member.lower_gms_inferior;
            }
        }
        return current_member;
    }

    /*
    Reorganization cases:
    1) Left subtree of reorganizing member is higher than the right subtree, left subtree of the reorganizing member's left inferior is equal or higher than the right subtree of the reorganizing member's left inferior
    2) Right subtree of reorganizing member is higher than the left subtree, right subtree of the reorganizing member's right inferior is equal or higher than the left subtree of the reorganizing member's right inferior
    3) Left subtree of reorganizing member is higher than the right subtree, right subtree of the reorganizing member's left inferior is higher than the left subtree of the reorganizing member's left inferior
    4) Right subtree of reorganizing member is higher than the left subtree, left subtree of the reorganizing member's right inferior is higher than the right subtree of the reorganizing member's right inferior
     */
    public static void reorganize (Member reorganizer){
        int left_subtree_height = Member.height(reorganizer.lower_gms_inferior);
        int right_subtree_height = Member.height(reorganizer.higher_gms_inferior);

        if (left_subtree_height - right_subtree_height > 1){        // case 1 or 3

            if (Member.height(reorganizer.lower_gms_inferior.lower_gms_inferior) >= Member.height(reorganizer.lower_gms_inferior.higher_gms_inferior)){      // case 1
                right_rotation(reorganizer);

                Member height_update = reorganizer;    // heights might change from over here
                while (height_update != null){
                    height_updater(height_update);
                    height_update = height_update.superior;
                }
            }


            else{       // case 3

                left_rotation(reorganizer.lower_gms_inferior);
                right_rotation(reorganizer);

                height_updater(reorganizer);
                height_updater(reorganizer.superior.lower_gms_inferior);
                Member height_update = reorganizer.superior;
                while (height_update != null){
                    height_updater(height_update);
                    height_update = height_update.superior;
                }
            }
        }

        else if (right_subtree_height - left_subtree_height > 1){       // case 2 or 4

            if (Member.height(reorganizer.higher_gms_inferior.higher_gms_inferior) >= Member.height(reorganizer.higher_gms_inferior.lower_gms_inferior)){      // case 2

                left_rotation(reorganizer);

                Member height_update = reorganizer;
                while (height_update != null){
                    height_updater(height_update);
                    height_update = height_update.superior;
                }
            }

            else{       // case 4

                right_rotation(reorganizer.higher_gms_inferior);
                left_rotation(reorganizer);

                height_updater(reorganizer);
                height_updater(reorganizer.superior.higher_gms_inferior);
                Member height_update = reorganizer.superior;
                while (height_update != null){
                    height_updater(height_update);
                    height_update = height_update.superior;
                }
            }
        }
    }


    public static void height_updater(Member member){
        member.height = Math.max(Member.height(member.lower_gms_inferior), Member.height(member.higher_gms_inferior)) + 1;
    }

    public static boolean division_analysis(Member current_member){     // reach the leaves and take them, take the current member if its superiors aren't taken already
        if (current_member == null){
            return false;
        }
        if (current_member.height == 0){
            Member.division_analysis_result += 1;
            return true;
        }
        boolean is_lower_taken = division_analysis(current_member.lower_gms_inferior);
        boolean is_higher_taken = division_analysis(current_member.higher_gms_inferior);
        if (!is_higher_taken && !is_lower_taken){
            Member.division_analysis_result += 1;
            return true;
        }
        else {
            return false;
        }
    }

    public static ArrayList<Member> rank_search(int rank, Member current_member, ArrayList<Member> result){     // decrement the rank in each recursive call, take the member when 0 is reached
        if (rank == 0){
            result.add(current_member);
            return result;
        }
        // calling lower first will result in a sorted array list
        if (Member.height(current_member.lower_gms_inferior) >= rank - 1){  // we can overlook the subtree if its height isn't enough to decrement rank to 0
            rank_search(rank - 1, current_member.lower_gms_inferior, result);
        }
        if (Member.height(current_member.higher_gms_inferior) >= rank - 1){
            rank_search(rank - 1, current_member.higher_gms_inferior, result);
        }
        return result;
    }

    public static void left_rotation (Member replaced){
        Member replacer = replaced.higher_gms_inferior;
        if (replaced.superior != null){
            if (replaced.superior.gms > replaced.gms){
                replaced.superior.lower_gms_inferior = replacer;
            }
            else{
                replaced.superior.higher_gms_inferior = replacer;
            }
        }
        else{
            boss = replacer;
        }
        replaced.higher_gms_inferior = replacer.lower_gms_inferior;
        replacer.superior = replaced.superior;
        replaced.superior = replacer;

        try{
            replacer.lower_gms_inferior.superior = replaced;
        }
        catch (NullPointerException e){}
        replacer.lower_gms_inferior = replaced;

    }

    public static void right_rotation (Member replaced){
        Member replacer = replaced.lower_gms_inferior;
        if (replaced.superior != null){
            if (replaced.superior.gms > replaced.gms){
                replaced.superior.lower_gms_inferior = replacer;
            }
            else{
                replaced.superior.higher_gms_inferior = replacer;
            }
        }
        else{
            boss = replacer;
        }
        replaced.lower_gms_inferior = replacer.higher_gms_inferior;
        replacer.superior = replaced.superior;
        replaced.superior = replacer;
        try{
            replacer.higher_gms_inferior.superior = replaced;
        }
        catch (NullPointerException e){}
        replacer.higher_gms_inferior = replaced;
    }
}
