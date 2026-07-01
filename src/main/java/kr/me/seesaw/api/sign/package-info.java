/**
 * <h2>도메인 기능 단위 패키지</h2>
 * <p>
 * 이 패키지는 하나의 도메인 또는 기능 영역에 대응되는 API 모듈의 표준 패키지 구조를 설명하기 위한 샘플입니다.
 * 실제 도메인 패키지를 만들 때는 {@code sampledomainname}을 도메인 이름으로 변경하고, 하위 패키지의 책임을 유지합니다.
 * </p>
 *
 * <h3>패키지 구성 원칙</h3>
 * <ul>
 * <li>{@code application}: 유스케이스를 조율하는 서비스와 인터페이스를 둡니다.</li>
 * <li>{@code context}: 요청 스코프에서 조회 결과를 캐싱하거나 화면/요청 처리에 필요한 컨텍스트를 제공합니다.</li>
 * <li>{@code dto}: 클라이언트 또는 계층 간 데이터 전송을 위한 {@code Request}, {@code Response} 객체를 둡니다.</li>
 * <li>{@code event}: 도메인 또는 애플리케이션 이벤트와 이벤트 리스너를 둡니다.</li>
 * <li>{@code presentation}: HTTP 요청을 받는 Controller 등 표현 계층 객체를 둡니다.</li>
 * </ul>
 *
 * <h3>배치 기준</h3>
 * <p>
 * 특정 도메인 유스케이스가 주도하는 코드는 해당 도메인 패키지에 둡니다.
 * 여러 도메인을 조합하는 조회 모델, 인터셉터, 컨텍스트는 별도의 composition 성격 패키지로 분리할 수 있습니다.
 * 설정, 보안, 예외 처리, 필터처럼 도메인에 종속되지 않는 프레임워크 코드는 {@code framework} 패키지에 둡니다.
 * </p>
 */
@NonNullApi
package kr.me.seesaw.api.sign;

import org.springframework.lang.NonNullApi;
