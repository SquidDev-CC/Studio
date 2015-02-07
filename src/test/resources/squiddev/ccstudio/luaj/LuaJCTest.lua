do
end

do
	-- Validate while loops
	local a, b = 1, 1
	while a < 100 do
		a, b = a + b, a
	end

	equals(144, a)
end

do
	-- Check scoping
	local a = "Hello"
	do
		local a = "Thing"
	end
	equals("Hello", a)
end

do
	-- Basic recursive functions and upvalues
	local factorial
	factorial = function(a)
		if a == 1 then return 1 end
		return a * factorial(a)
	end

	equals(720, factorial(6))
end

do
	-- Upvalues
	local a = "Hello"
	local function increase()
		if a == "Hello" then
			a = 1
		else
			a = a + 1
		end
	end

	increase()
	equals(1, a)

	increase()
	equals(1, a)
end

-- Check some return values
return "hello", "world"
