/**
 * <h2>계층 간 데이터 전송 객체 (DTO) 패키지</h2>
 * <p>
 * 이 패키지는 도메인 간 또는 클라이언트와의 통신을 위한 DTO들을 포함합니다.
 * 모든 DTO는 안전한 데이터 처리를 위해 {@code record}와 Builder 패턴을 활용하여 <strong>불변 객체</strong>로 구성합니다.
 * </p>
 *
 * <h3>📌 접미어(Suffix) 명명 규칙</h3>
 * <ul>
 * <li><strong>요청 객체:</strong> {@code Request} 접미어 사용</li>
 * <li><strong>응답 객체:</strong> {@code Response} 접미어 사용</li>
 * </ul>
 *
 * <h3>📌 요청 객체(Request) 분류 및 명명 규칙</h3>
 * <p>
 * 요청 객체는 성격에 따라 <strong>질의 객체(Query Object)</strong>와 <strong>명령 객체(Command Object)</strong>로 분류하며,
 * 명명 규칙은 <code>{Action}{Domain}{Suffix}</code> 패턴을 따릅니다.
 * </p>
 * <ul>
 * <li>{@code SearchEventsRequest} - 이벤트 검색 (Query)</li>
 * <li>{@code CreateSiteRequest} - 사이트 생성 (Command)</li>
 * <li>{@code UpdateSiteRequest} - 사이트 수정 (Command)</li>
 * </ul>
 */
package kr.me.seesaw.api.category.dto;

