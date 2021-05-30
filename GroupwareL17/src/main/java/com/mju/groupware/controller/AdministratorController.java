package com.mju.groupware.controller;

import java.io.IOException;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.mju.groupware.constant.ConstantAdmin;
import com.mju.groupware.dto.Professor;
import com.mju.groupware.dto.Student;
import com.mju.groupware.dto.User;
import com.mju.groupware.dto.UserList;
import com.mju.groupware.function.UserInfoMethod;
import com.mju.groupware.service.AdminService;
import com.mju.groupware.service.ProfessorService;
import com.mju.groupware.service.StudentService;
import com.mju.groupware.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdministratorController {

	@Autowired
	private AdminService adminService;
	@Autowired
	private UserService userService;
	@Autowired
	private StudentService studentService;
	@Autowired
	private ProfessorService professorService;
	@Autowired
	private UserInfoMethod userInfoMethod;

	private String UserName;
	private String UserLoginID;
	private String MysqlID;

	// constant연결
	private ConstantAdmin constantAdmin;
	private String PRole;
	private String SRole;
	private String ARole;
	private String TemporaryPwd;

	private String Home;
	private String List;
	private String ReList;
	private String ReSleep;
	private String SleepList;
	private String SecessionList;
	private String ReSecessionList;
	private String Detail;
	private String SDetail;
	private String PDetail;
	private String ReSDetail;
	private String RePDetail;
	private String SModify;
	private String PModify;
	private String SManage;
	private String PManage;
	private String SManageModify;
	private String PManageModify;

	public AdministratorController() {
		GenericXmlApplicationContext Ctx = new GenericXmlApplicationContext();
		Ctx.load("classpath:/xmlForProperties/Admin.xml");
		Ctx.refresh();
		// 빈 객체 받아오기
		this.constantAdmin = (ConstantAdmin) Ctx.getBean("admin");
	}

	// 관리자 로그인 완료 화면
	@RequestMapping(value = "/homeAdmin", method = RequestMethod.GET)
	public String homeAdmin(User user, Principal Principal, Model model, HttpServletRequest request) {
		String UserLoginID = Principal.getName();// 로그인 한 아이디
		ArrayList<String> Info = new ArrayList<String>();
		Info = userService.SelectUserProfileInfo(UserLoginID);

		// 관리자 이름
		UserName = Info.get(0);
		model.addAttribute("UserName", UserName);

		Date Now = new Date();
		SimpleDateFormat Date = new SimpleDateFormat("yyyy-MM-dd");
		user.setDate(Date.format(Now));
		userService.UpdateLoginDate(user);
		this.Home = this.constantAdmin.getHome();
		return Home;

	}

	// 관리자메뉴 - user list
	@RequestMapping(value = "/manageList", method = RequestMethod.GET)
	public String manageList(Model model, Principal Principal, User user) {

		try {
			String LoginID = Principal.getName();// 로그인 한 아이디
			ArrayList<String> SelectUserProfileInfo = new ArrayList<String>();
			SelectUserProfileInfo = userService.SelectUserProfileInfo(LoginID);
			user.setUserName(SelectUserProfileInfo.get(0));
			// 학생 이름
			UserName = SelectUserProfileInfo.get(0);
			model.addAttribute("UserName", UserName);

			List<UserList> SelectUserList = adminService.SelectUserlist();
			model.addAttribute("list", SelectUserList);
			// 여기서 선택된 학생번호 받아오시면됩니다.
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.List = this.constantAdmin.getList();
		return List;
	}

	// 관리자메뉴 - 관리자 권한으로 user 권한 변경
	@ResponseBody
	@RequestMapping(value = "/manageList.do")
	public String changeAuth(RedirectAttributes redirectAttributes, Model model, HttpServletRequest request,
			HttpServletResponse response) {

		String OptionValue = request.getParameter("OptionValue");
		String[] AjaxMsg = request.getParameterValues("CheckArr");

		this.PRole = this.constantAdmin.getPRole();
		this.SRole = this.constantAdmin.getSRole();
		this.ARole = this.constantAdmin.getARole();

		String URole = "ROLE_USER";

		if (OptionValue.equals("ROLE_SUSER")) {
			OptionValue = URole;
		} else if (OptionValue.equals("ROLE_PUSER")) {
			OptionValue = URole;
		} else if (OptionValue.equals("ROLE_ADMIN")) {
			OptionValue = ARole;
		}
		for (int i = 0; i < AjaxMsg.length; i++) {
			if (OptionValue.equals(ARole)) {
				userService.UpdateAdminRole(AjaxMsg[i], OptionValue);
			} else if (OptionValue.equals(URole)) {
				userService.UpdateUserRole(AjaxMsg[i], OptionValue);
			}
		}
		this.List = this.constantAdmin.getList();
		return List;
	}

	// 관리자 메뉴 - 관리자 권한으로 user 탈퇴
	@ResponseBody
	@RequestMapping(value = "/withdrawal.do")
	public String DoWithdrawlByAdmin(HttpServletRequest request, User user, Student student, Professor professor) {

		String[] AjaxMsg = request.getParameterValues("CheckArr");
		int Size = AjaxMsg.length;
		for (int i = 0; i < Size; i++) {
			User UserInfo = userService.SelectUserInfo(AjaxMsg[i]);
			user.setUserLoginID(UserInfo.getUserLoginID());
			user.setEnabled(false);
			user.setWithdrawal(true);
			// 탈퇴한 날짜 set
			Date Now = new Date();
			SimpleDateFormat Date = new SimpleDateFormat("yyyy-MM-dd");
			user.setDate(Date.format(Now));
			// 탈퇴 업데이트
			userService.UpdateWithdrawal(user);

		}
		this.ReList = this.constantAdmin.getReList();
		return ReList;
	}

	// 관리자 휴면 메뉴 - 관리자 권한으로 휴면 계정 탈퇴
	@ResponseBody
	@RequestMapping(value = "/dormantWithdrawal.do")
	public String DoDormantWithdrawalByAdmin(HttpServletRequest request, User user, Student student,
			Professor professor) {

		String[] AjaxMsg = request.getParameterValues("CheckArr");
		int Size = AjaxMsg.length;
		for (int i = 0; i < Size; i++) {
			userService.UpdateWithdrawalByDormant(AjaxMsg[i]);
		}
		this.ReSleep = this.constantAdmin.getReSleep();
		return ReSleep;
	}

	/* 관리자 메뉴-휴면 계정 관리 화면 */
	@RequestMapping(value = "/manageSleep", method = RequestMethod.GET)
	public String manageSleep(Model model) {

		try {
			List<UserList> SelectDormantUserList = adminService.SelectDormantUserList();
			model.addAttribute("DormantList", SelectDormantUserList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.SleepList = this.constantAdmin.getSleepList();
		return SleepList;
	}

	// 관리자 휴면 메뉴 - 관리자 권한으로 휴면 계정 복구
	@ResponseBody
	@RequestMapping(value = "/dormantRecovery.do")
	public String DoDormantRecoveryByAdmin(HttpServletRequest request) {

		String[] AjaxMsg = request.getParameterValues("CheckArr");
		int Size = AjaxMsg.length;
		for (int i = 0; i < Size; i++) {
			// 쿼리문 작동
			userService.UpdateDormantOneToZero(AjaxMsg[i]);
		}
		this.ReSleep = this.constantAdmin.getReSleep();
		return ReSleep;
	}

	/* 관리자 메뉴-탈퇴 계정 관리 화면 */
	@RequestMapping(value = "/manageSecession", method = RequestMethod.GET)
	public String manageSecession(Model model) {
		try {
			List<UserList> SelectWithdrawalUserList = adminService.SelectWithdrawalUserList();
			model.addAttribute("SelectWithdrawalUserList", SelectWithdrawalUserList);
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.SecessionList = this.constantAdmin.getSecessionList();
		return SecessionList;
	}

	// 관리자 탈퇴 메뉴 - 관리자 권한으로 탈퇴 계정 복구
	@ResponseBody
	@RequestMapping(value = "/withdrawalRecovery.do")
	public String DoWithdrawalRecoveryByAdmin(HttpServletRequest request, User user) {

		String[] AjaxMsg = request.getParameterValues("CheckArr");
		int Size = AjaxMsg.length;
		for (int i = 0; i < Size; i++) {
			userService.UpdateDoWithdrawalRecoveryByAdmin(AjaxMsg[i]);
		}
		this.ReSecessionList = this.constantAdmin.getSecessionList();
		return ReSecessionList;
	}

	// 관리자 메뉴에서 회원 아이디, 이름 클릭 시 회원 role에 따라 페이지 리턴
	@RequestMapping(value = "/detail", method = RequestMethod.GET)
	public String detail(HttpServletRequest request, Model model, HttpServletResponse response, RedirectAttributes rttr)
			throws IOException {
		MysqlID = request.getParameter("no");
		String MysqlRole = request.getParameter("R");
		String UserAuthority = request.getParameter("A");

		String UAuthority = "ROLE_USER";
		String AAuthority = "ROLE_ADMIN";

		this.Detail = this.constantAdmin.getDetail();

		this.ARole = this.constantAdmin.getARole();
		this.SRole = this.constantAdmin.getSRole();
		this.PRole = this.constantAdmin.getPRole();

		this.ReSDetail = this.constantAdmin.getReSDetail();
		this.RePDetail = this.constantAdmin.getRePDetail();

		if (MysqlRole.equals(SRole) && UserAuthority.equals(UAuthority)) {
			return ReSDetail;
		} else if (MysqlRole.equals(PRole) && UserAuthority.equals(UAuthority)) {
			return RePDetail;
		} else if (UserAuthority.equals(AAuthority)) {
			rttr.addFlashAttribute("DONT", "true");
			this.ReList = this.constantAdmin.getReList();
			return ReList;
		} else {
			return ReList;
		}
	}

	// 관리자 메뉴에서 회원 아이디, 이름 클릭 시 학생 정보 출력
	@RequestMapping(value = "/detailStudent", method = RequestMethod.GET)
	public String detailStudent(HttpServletRequest request, Model model, Principal principal) {

		ArrayList<String> SelectUserProfileInfo = new ArrayList<String>();
		SelectUserProfileInfo = userService.SelectUserProfileInfoByID(MysqlID);

		// 학번
		UserLoginID = SelectUserProfileInfo.get(0);
		// 이름
		String UserName = SelectUserProfileInfo.get(1);
		// 전화번호
		String UserPhoneNum = SelectUserProfileInfo.get(2);
		// 이메일
		String UserEmail = SelectUserProfileInfo.get(3);
		// 정보공개
		String OpenName = SelectUserProfileInfo.get(4);
		String OpenEmail = SelectUserProfileInfo.get(5);
		String OpenPhoneNum = SelectUserProfileInfo.get(6);
		String OpenMajor = SelectUserProfileInfo.get(7);
		String OpenGrade = SelectUserProfileInfo.get(8);
		// 단과대학
		String StudentColleges = SelectUserProfileInfo.get(9);
		// 전공
		String StudentMajor = SelectUserProfileInfo.get(10);
		// 학년
		String StudentGrade = SelectUserProfileInfo.get(11);
		// 부전공
		String StudentDoubleMajor = SelectUserProfileInfo.get(12);
		// 성별
		String StudentGender = SelectUserProfileInfo.get(13);
		/*-------------------------------------------------------------------------------------------*/
		// 학번추가
		model.addAttribute("UserLoginID", UserLoginID);
		// 이름
		model.addAttribute("UserName", UserName);
		// 성별
		model.addAttribute("StudentGender", StudentGender);
		// 연락처
		model.addAttribute("UserPhoneNum", UserPhoneNum);
		// 학년
		model.addAttribute("StudentGrade", StudentGrade);
		// 단과대학
		model.addAttribute("StudentColleges", StudentColleges);
		// 전공
		model.addAttribute("StudentMajor", StudentMajor);
		// 부전공
		model.addAttribute("StudentDoubleMajor", StudentDoubleMajor);
		// 이메일
		model.addAttribute("UserEmail", UserEmail);

		// 정보공개여부
		String Result = "Error";
		Result = OpenName + "," + OpenEmail + "," + OpenPhoneNum + "," + OpenMajor + "," + OpenGrade;
		if (Result.contains(",비공개") || Result.contains("비공개")) {
			Result = Result.replaceAll(",비공개", "");
			Result = Result.replaceAll("비공개", "");
			boolean startComma = Result.startsWith(",");
			boolean endComma = Result.endsWith(",");
			if (startComma || endComma) {
				Result = Result.substring(Result.length() - (Result.length() - 1), Result.length());
			}
		}
		if (!Result.equals("Error")) {
			model.addAttribute("StudentInfoOpen", Result);
		}
		this.SDetail = this.constantAdmin.getSDetail();
		return SDetail;

	}

	// 관리자 메뉴에서 회원 아이디, 이름 클릭 시 교수 정보 출력
	@RequestMapping(value = "/detailProfessor", method = RequestMethod.GET)
	public String detailProfessor(HttpServletRequest request, Model model, Principal principal) {
		ArrayList<String> SelectUserProfileInfo = new ArrayList<String>();
		SelectUserProfileInfo = userService.SelectUserProfileInfoByID(MysqlID);
		// 학번
		UserLoginID = SelectUserProfileInfo.get(0);
		// 이름
		String UserName = SelectUserProfileInfo.get(1);
		// 전화번호
		String UserPhoneNum = SelectUserProfileInfo.get(2);
		// 이메일
		String UserEmail = SelectUserProfileInfo.get(3);
		// 정보공개
		String OpenName = SelectUserProfileInfo.get(4);
		String OpenEmail = SelectUserProfileInfo.get(5);
		String OpenPhoneNum = SelectUserProfileInfo.get(6);
		String OpenMajor = SelectUserProfileInfo.get(7);
		String OpenGrade = SelectUserProfileInfo.get(8);
		// 단과대학
		String ProfessorColleges = SelectUserProfileInfo.get(14);
		// 전공
		String ProfessorMajor = SelectUserProfileInfo.get(15);
		// 교수실
		String ProfessorRoom = SelectUserProfileInfo.get(16);
		// 교수실 전화번호
		String ProfessorRoomNum = SelectUserProfileInfo.get(17);
		/*-------------------------------------------------------------------------------------------*/
		// 학번추가
		model.addAttribute("UserLoginID", UserLoginID);
		// 이름
		model.addAttribute("UserName", UserName);
		// 연락처
		model.addAttribute("UserPhoneNum", UserPhoneNum);
		// 단과대학
		model.addAttribute("ProfessorColleges", ProfessorColleges);
		// 전공
		model.addAttribute("ProfessorMajor", ProfessorMajor);
		// 교수실
		model.addAttribute("ProfessorRoom", ProfessorRoom);
		// 교수실 전화번호
		model.addAttribute("ProfessorRoomNum", ProfessorRoomNum);
		// 이메일
		model.addAttribute("UserEmail", UserEmail);

		// 정보공개여부
		String Result = "Error";
		Result = OpenName + "," + OpenEmail + "," + OpenPhoneNum + "," + OpenMajor + "," + OpenGrade;
		if (Result.contains(",비공개") || Result.contains("비공개")) {
			Result = Result.replaceAll(",비공개", "");
			Result = Result.replaceAll("비공개", "");
			boolean startComma = Result.startsWith(",");
			boolean endComma = Result.endsWith(",");
			if (startComma || endComma) {
				Result = Result.substring(Result.length() - (Result.length() - 1), Result.length());
			}
		}
		if (!Result.equals("Error")) {
			model.addAttribute("ProfessorInfoOpen", Result);
		}

		this.PDetail = this.constantAdmin.getPDetail();
		return PDetail;
	}

	@RequestMapping(value = "/ModifyStudent", method = RequestMethod.POST)
	public String UpdateStudentInfo() {
		this.SModify = this.constantAdmin.getSModify();
		return SModify;
	}

	@RequestMapping(value = "/ModifyProfessor", method = RequestMethod.POST)
	public String UpdateProfessorInfo() {
		this.PModify = this.constantAdmin.getPModify();
		return PModify;
	}

	/* 관리자 메뉴-회원 목록 클릭 시 정보 출력 화면 */
	@RequestMapping(value = "/manageStudent", method = RequestMethod.GET)
	public String manageStudent() {
		this.SManage = this.constantAdmin.getSManage();
		return SManage;
	}

	@RequestMapping(value = "/manageProfessor", method = RequestMethod.GET)
	public String manageProfessor() {
		this.PManage = this.constantAdmin.getPManage();
		return PManage;
	}

	@RequestMapping(value = "/manageModifyStudent", method = RequestMethod.GET)
	public String manageModifyStudent() {
		this.SManageModify = this.constantAdmin.getSManageModify();
		return SManageModify;

	}

	@RequestMapping(value = "/manageModifyStudent", method = RequestMethod.POST)
	public String DoManageModifyStudent(HttpServletRequest request, User user, Student student) {

		user.setUserLoginID(UserLoginID);
		student.setUserID(Integer.parseInt(MysqlID));
		if (request.getParameter("ModifyComplete") != null) {
			if (!((String) request.getParameter("UserName")).equals("")) {
				// 이름바꾸기
				user.setUserName((String) request.getParameter("UserName"));
				userService.UpdateUserName(user);
			}
			if (!((String) request.getParameter("StudentGender")).equals(" ")) {
				// 성별바꾸기
				student.setStudentGender((String) request.getParameter("StudentGender"));
				studentService.UpdateStudentGender(student);
			}
			if (!((String) request.getParameter("UserPhoneNum")).equals("")) {
				// 전화번호바꾸기
				user.setUserPhoneNum((String) request.getParameter("UserPhoneNum"));
				userService.updateUserPhoneNumber(user);
			}
			System.out.println(request.getParameter("StudentGrade"));
			if (!((String) request.getParameter("StudentGrade")).equals(" ")) {
				// 학년
				student.setStudentGrade((String) request.getParameter("StudentGrade"));
				studentService.updateStudentGrade(student);
			}
			if (!((String) request.getParameter("StudentColleges")).equals("")) {
				// 단과대학
				student.setStudentColleges((String) request.getParameter("StudentColleges"));
				studentService.UpdateStudentColleges(student);
			}
			if (!((String) request.getParameter("StudentMajor")).equals("")) {
				// 전공
				student.setStudentMajor((String) request.getParameter("StudentMajor"));
				studentService.UpdateStudentMajor(student);
			}

			if (((String) request.getParameter("member")).equals("Y")
					&& !request.getParameter("StudentDoubleMajor").equals(" ")) {
				// 부전공 있다
				System.out.println(7);
				student.setStudentDoubleMajor((String) request.getParameter("StudentDoubleMajor"));
				studentService.UpdateStudentDobuleMajor(student);
			} else if (((String) request.getParameter("member")).equals("N")) {
				// 부전공 없다
				student.setStudentDoubleMajor("없음");
				studentService.UpdateStudentDobuleMajor(student);
			}
		}
		this.PManageModify = this.constantAdmin.getPManageModify();
		return PManageModify;

	}

	@RequestMapping(value = "/manageModifyProfessor", method = RequestMethod.GET)
	public String manageModifyProfessor() {
		// this.PManageModify = this.constantAdmin.getPManageModify();
		// return PManageModify;
		return "/admin/manageModifyProfessor";

	}

	@RequestMapping(value = "/manageModifyProfessor", method = RequestMethod.POST)
	public String DoManageModifyProfessor(HttpServletRequest request, User user, Professor professor) {

		user.setUserLoginID(UserLoginID);
		professor.setUserID(Integer.parseInt(MysqlID));
		if (request.getParameter("ModifyCompleteP") != null) {
			if (!((String) request.getParameter("UserName")).equals("")) {
				// 이름바꾸기
				user.setUserName((String) request.getParameter("UserName"));
				userService.UpdateUserName(user);
			}
			if (!((String) request.getParameter("UserPhoneNum")).equals("")) {
				// 전화번호바꾸기
				user.setUserPhoneNum((String) request.getParameter("UserPhoneNum"));
				userService.updateUserPhoneNumber(user);
			}
			if (!((String) request.getParameter("ProfessorColleges")).equals(" ")) {
				// 단과대학
				professor.setProfessorColleges((String) request.getParameter("ProfessorColleges"));
				professorService.UpdateProfessorColleges(professor);
			}
			if (!((String) request.getParameter("ProfessorMajor")).equals(" ")) {
				// 전공
				professor.setProfessorMajor((String) request.getParameter("ProfessorMajor"));
				professorService.UpdateProfessorMajor(professor);
			}
			if (!((String) request.getParameter("ProfessorRoom")).equals(" ")) {
				// 교수실
				professor.setProfessorRoom((String) request.getParameter("ProfessorRoom"));
				professorService.UpdateProfessorRoom(professor);
			}
			if (!((String) request.getParameter("ProfessorRoomNum")).equals(" ")) {
				// 교수실 전화번호
				professor.setProfessorRoomNum((String) request.getParameter("ProfessorRoomNum"));
				professorService.UpdateProfessorRoomNum(professor);
			}
		}
		// this.SManageModify = this.constantAdmin.getSManageModify();
		// return SManageModify;
		return "/admin/manageModifyStudent";

	}

	private void GetUserInformation(Principal principal, User user, Model model) {
		String LoginID = principal.getName();// 로그인 한 아이디
		ArrayList<String> SelectUserProfileInfo = new ArrayList<String>();
		SelectUserProfileInfo = userService.SelectUserProfileInfo(LoginID);
		user.setUserLoginID(LoginID);
		if (SelectUserProfileInfo.get(2).equals("STUDENT")) {
			ArrayList<String> StudentInfo = new ArrayList<String>();
			StudentInfo = studentService.SelectStudentProfileInfo(SelectUserProfileInfo.get(1));
			userInfoMethod.StudentInfo(model, SelectUserProfileInfo, StudentInfo);
		} else if (SelectUserProfileInfo.get(2).equals("PROFESSOR")) {
			ArrayList<String> ProfessorInfo = new ArrayList<String>();
			ProfessorInfo = professorService.SelectProfessorProfileInfo(SelectUserProfileInfo.get(1));
			userInfoMethod.ProfessorInfo(model, SelectUserProfileInfo, ProfessorInfo);
		} else if (SelectUserProfileInfo.get(2).equals("ADMINISTRATOR")) {
			userInfoMethod.AdministratorInfo(model, SelectUserProfileInfo);
		}
	}

}