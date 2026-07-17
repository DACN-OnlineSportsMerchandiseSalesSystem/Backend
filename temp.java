import java.util.HashMap;
import java.util.Scanner;

public class temp {
    public static void main(String[] args) {
        // Scanner scanner = new Scanner(System.in);
        // String s = scanner.nextLine();
        // String t = scanner.nextLine();
        // String ans = isIsomorphic(s, t) ? "true" : "false";
        // System.out.println(ans);
        // scanner.close();
        // int[] arr = { 0 };
        // int target = 0;
        // binarySearch(arr, target);
        // binarySearch(arr, target, 0, arr.length - 1);
        class Base{
        public:
            void show(){ System.out.println("Show Base");}
        }
        class Derived extends Base{
        public:
            void show(){ System.out.println("Show Derived");}
        }
        Base *ptr;
        Derived d;
        ptr = &d;
        ptr->show();
    }
v
    public static void write(int i) {
        for (int j = 0; j < i; j++) {
            System.out.println("yêu An nhìu heheeeeeee");
        }
    }

    public static boolean isIsomorphic(String s, String t) {
        if (s.length() != t.length())
            return false;

        HashMap<Character, Character> map = new HashMap<>();
        for (int i = 0; i < s.length(); i++) {
            if (map.containsKey(s.charAt(i))) {
                if (map.get(s.charAt(i)) != t.charAt(i))
                    return false;
            } else {
                if (map.containsValue(t.charAt(i)))
                    return false;
                map.put(s.charAt(i), t.charAt(i));
            }
        }
        return true;
    }

    public static void binarySearch(int[] arr, int target) {
        int left = 0, right = arr.length - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (arr[mid] == target) {
                System.out.println("Element found at index " + mid);
                return;
            }
            if (arr[mid] < target) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        System.out.println("Element not found");
    }

    public static void binarySearch(int[] arr, int target, int left, int right) {
        if (left > right) {
            System.out.println("Element not found");
            return;
        }
        int mid = left + (right - left) / 2;
        if (arr[mid] == target) {
            System.out.println("Element found at index " + mid);
            return;
        }
        if (arr[mid] < target) {
            binarySearch(arr, target, mid + 1, right);
        } else {
            binarySearch(arr, target, left, mid - 1);
        }
    }
}
