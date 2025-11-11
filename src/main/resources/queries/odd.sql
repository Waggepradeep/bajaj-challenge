SELECT e.department_id,
       e.name AS employee_name,
       e.salary
FROM employee e
WHERE e.salary = (
    SELECT MAX(e2.salary)
    FROM employee e2
    WHERE e2.department_id = e.department_id
);

--SELECT 1;