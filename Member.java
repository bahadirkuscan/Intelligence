public class Member {
    public String name;
    public double gms;
    public Member superior;
    public Member lower_gms_inferior;
    public Member higher_gms_inferior;
    public int height;
    public static int division_analysis_result = 0;

    Member(String name, double gms, Member superior, int height){
        this.name = name;
        this.gms = gms;
        this.superior = superior;
        this.height = height;
    }

    public static int height(Member member){
        if (member == null){
            return -1;
        }
        else{
            return member.height;
        }
    }
}
