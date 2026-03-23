//package beetech.app.core.interfaces;
//
//import java.util.List;
//
//import corerfidclient.dto.Command;
//import corerfidclient.dto.SgtinResult;
//import retrofit2.Call;
//import retrofit2.http.GET;
//import retrofit2.http.Path;
//
//public interface ApiService {
//    @GET("utils/epc2sgtin/{epc}")
//    Call<SgtinResult> epc2sgtin(@Path("epc") String epc);
//    @GET("command/GetReaderCommandList")
//    Call<List<Command>> getCommands();
//}
