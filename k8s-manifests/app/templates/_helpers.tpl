{{- define "jdbcApp.driverClass" -}}
{{- if eq .Values.db "postgresql" -}}org.postgresql.Driver
{{- else if eq .Values.db "mysql" -}}com.mysql.cj.jdbc.Driver
{{- else if eq .Values.db "mariadb" -}}org.mariadb.jdbc.Driver
{{- else -}}org.unknown.Driver
{{- end -}}
{{- end -}}
