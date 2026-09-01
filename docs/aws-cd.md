# AWS S3 및 CD 설정

이 프로젝트는 운영 기본값으로 기사 백업 저장소를 S3로 사용합니다. 로컬 실행은 `local` 프로필에서 파일 저장소를 사용합니다.

## 1. S3 버킷

운영에는 두 버킷을 분리해서 사용합니다.

- 기사 백업 버킷: 애플리케이션이 `article-backups/*.jsonl`을 저장하고 복구할 때 사용합니다.
- CodeDeploy 배포 버킷: GitHub Actions가 배포 zip 파일을 올리고 CodeDeploy가 EC2로 내려받을 때 사용합니다.

권장 설정:

- Block Public Access 활성화
- Bucket Versioning 활성화
- 기본 암호화 활성화
- 리전은 기본값 `ap-northeast-2` 또는 `AWS_REGION`으로 통일

## 2. GitHub Secrets

`.github/workflows/cd.yml`은 `dev` 브랜치 push 또는 수동 실행으로 배포됩니다.

필요한 Secrets:

- `AWS_ROLE_TO_ASSUME`: GitHub Actions OIDC로 assume할 IAM Role ARN
- `AWS_REGION`: 예: `ap-northeast-2`
- `AWS_CODEDEPLOY_BUCKET`: 배포 zip을 올릴 S3 버킷명
- `CODEDEPLOY_APPLICATION_NAME`: CodeDeploy Application 이름
- `CODEDEPLOY_DEPLOYMENT_GROUP_NAME`: CodeDeploy Deployment Group 이름

GitHub Actions Role 최소 권한 예시:

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": ["s3:PutObject"],
      "Resource": "arn:aws:s3:::YOUR_CODEDEPLOY_BUCKET/monu/*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "codedeploy:CreateDeployment",
        "codedeploy:GetApplication",
        "codedeploy:GetApplicationRevision",
        "codedeploy:GetDeployment",
        "codedeploy:GetDeploymentGroup"
      ],
      "Resource": "*"
    }
  ]
}
```

## 3. EC2 설정

EC2에는 CodeDeploy Agent, Java 17, `curl`이 필요합니다. 인스턴스 프로필에는 아래 권한이 필요합니다.

- CodeDeploy 배포 버킷: `s3:GetObject`, `s3:GetObjectVersion`
- 기사 백업 버킷: `s3:PutObject`, `s3:GetObject`

애플리케이션 환경값은 EC2의 `/etc/monu/monu.env`에 둡니다. 최초 배포 시 파일이 없으면 기본 파일만 생성되므로, 실제 값은 서버에서 채워야 합니다.

예시:

```env
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://YOUR_DB_HOST:5432/YOUR_DB_NAME
SPRING_DATASOURCE_USERNAME=YOUR_DB_USER
SPRING_DATASOURCE_PASSWORD=YOUR_DB_PASSWORD
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_BATCH_JOB_ENABLED=false
BATCH_ENABLED=true
BATCH_SCHEDULER_ENABLED=true
AWS_S3_BUCKET=YOUR_ARTICLE_BACKUP_BUCKET
AWS_REGION=ap-northeast-2
NAVER_CLIENT_ID=YOUR_NAVER_CLIENT_ID
NAVER_CLIENT_SECRET=YOUR_NAVER_CLIENT_SECRET
SERVER_PORT=8080
JAVA_OPTS=-Xms256m -Xmx512m
```

## 4. 배포 동작

GitHub Actions는 테스트와 `bootJar` 빌드를 실행한 뒤 `app.jar`, `appspec.yml`, `scripts/`를 zip으로 묶어 S3에 업로드합니다. 이후 CodeDeploy가 EC2의 `/opt/monu`로 파일을 복사하고 `monu.service`를 systemd 서비스로 실행합니다.

배포 후 헬스 체크는 `/actuator/health`를 호출합니다.

## 5. 배포 실패 점검

CodeDeploy에서 `Failing in-progress lifecycle event after an agent restart`가 표시되면 EC2 인스턴스 안에서 아래 항목을 먼저 확인합니다.

```bash
sudo systemctl status codedeploy-agent --no-pager --full
sudo journalctl -u codedeploy-agent -n 200 --no-pager
sudo tail -n 200 /var/log/aws/codedeploy-agent/codedeploy-agent.log
sudo tail -n 200 /opt/codedeploy-agent/deployment-root/deployment-logs/codedeploy-agent-deployments.log
sudo systemctl status monu.service --no-pager --full
sudo journalctl -u monu.service -n 200 --no-pager
```

`monu.service`가 시작 직후 종료되면 `/etc/monu/monu.env`에 운영 필수 값이 있는지 확인합니다. 최소한 `MONGODB_URI`, `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `AWS_S3_BUCKET`, `NAVER_CLIENT_ID`, `NAVER_CLIENT_SECRET`은 실제 운영 값으로 채워져야 합니다.
