package edu.ecut.servlet;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.malajava.util.JdbcHelper;
import org.malajava.util.StringHelper;

import edu.ecut.entity.Explain;
import edu.ecut.entity.Topic;
import edu.ecut.entity.User;

@WebServlet( "/detail.do" )
public class DetailServlet  extends HttpServlet {

	private static final long serialVersionUID = -3202278077673657729L;

	@Override
	protected void service( HttpServletRequest request , HttpServletResponse response ) 
			throws ServletException, IOException {
		
		// 从 请求中获得请求参数的值
		String id = request.getParameter( "id" );
		
		if( StringHelper.notEmpty( id ) ){
			
			try {
				int topicId = Integer.parseInt( id ) ; // 将字符串按照 十进制 解析问 int 类型数值
				
				// 根据得到的 问题的 主键 查询数据库，得到 详细信息
				final String SQL = "SELECT  id , title , content , publish_time , publish_ip , user_id  FROM t_topic WHERE id = ? " ;
				
				ResultSet rs = JdbcHelper.query(SQL,  topicId );
				
				Topic t = null ;
				int userId = -1 ;
				
				if( rs.next() ){ // 当 根据 问题的主键 获取到 问题信息时
					t = new Topic(); // 创建 Topic 对象
					
					t.setId( rs.getInt( 1 ) ); // 将 结果集 中的 该行数据 封装到 t 对象的 id 属性中
					t.setTitle( rs.getString( 2 ) );
					t.setContent( rs.getString( 3 ) );
					t.setPublishTime( rs.getTimestamp( 4 ));
					t.setPublishIp( rs.getString( 5 ) );
					
					userId = rs.getInt( 6 ) ; 
				}
				
				JdbcHelper.release( rs ); // 关闭结果集
				
				// 获得提问者
				final String getUserSQL = "SELECT id , username , password FROM t_user WHERE id = ? " ;
				rs = JdbcHelper.query( getUserSQL,  userId );
				if( userId != -1 && rs.next() ){
					User u = new User();
					// 封装数据
					u.setId( rs.getInt( 1 ) );
					u.setUsername( rs.getString( 2 ));
					u.setPassword( rs.getString( 3 ) ) ;
					// 将获取到的用户数据设置到 Topic 对象的 user 属性中
					t.setUser( u );
				}
				
				JdbcHelper.release( rs ); // 关闭结果集
				
				// 获得当前的问题的所有解答
				String explainSQL = "SELECT  id , content , explain_time , explain_ip , user_id  from t_explain where topic_id = ? " ; 
				rs = JdbcHelper.query(explainSQL, topicId );
				int explainerId = -1 ;
				List<Explain> explains = new ArrayList<>();
				while( rs.next() ) {
					Explain e = new Explain();
					e.setId( rs.getInt( 1 ) );
					e.setContent( rs.getString( 2 ) );
					e.setExplainTime( rs.getTimestamp( 3 ) );
					e.setExplainIp( rs.getString( 4 ) );
					explainerId = rs.getInt( 5 ) ;
					explains.add( e ) ;
				}
				
				t.setExplains(explains); // 将解答设置到 Topic 对象上
				
				/*** 为 所有的解答，获取解答者的详细信息 ***/
				
				HttpSession session = request.getSession();
				session.setAttribute( "topic" , t );
				
				response.sendRedirect( request.getContextPath() + "/detail.html" );
				
				return ; // 让方法立即结束
			} catch (NumberFormatException e) {
				e.printStackTrace();
				//response.sendRedirect( request.getContextPath() + "/list.do" );
			} catch (SQLException e) {
				e.printStackTrace();
				//response.sendRedirect( request.getContextPath() + "/list.do" );
			}
			
		} else {
			//response.sendRedirect( request.getContextPath() + "/list.do" );
		}
		
		response.sendRedirect( request.getContextPath() + "/list.do" );
		
	}

}
