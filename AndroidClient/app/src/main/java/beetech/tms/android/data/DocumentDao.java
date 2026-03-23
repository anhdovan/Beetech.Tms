package beetech.tms.android.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY documentCode")
    List<DocumentEntity> getAll();

    @Query("SELECT * FROM documents WHERE rfidTag = :tag LIMIT 1")
    DocumentEntity findByTag(String tag);

    @Query("SELECT * FROM documents WHERE title LIKE '%' || :search || '%' OR documentCode LIKE '%' || :search || '%'")
    List<DocumentEntity> search(String search);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DocumentEntity> documents);

    @Query("DELETE FROM documents")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM documents")
    int count();

    @Query("SELECT COUNT(*) FROM documents WHERE tagStatus = 'Tagged' OR tagStatus = 'Verified'")
    int countTagged();
}
