// Interface ini memastikan setiap manager data memiliki fitur Load dan Save
public interface DataStorage {
    void loadData(); // Wajib implementasi cara baca data
    void saveData(); // Wajib implementasi cara simpan data
}