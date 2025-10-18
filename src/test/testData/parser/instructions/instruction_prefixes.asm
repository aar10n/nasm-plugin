; Instructions with prefixes
lock add dword [counter], 1
rep movsb
repne scasb
xacquire lock mov [mutex], 0
