import unittest

from util import validate


class TestValidationMethods(unittest.TestCase):
    def setUp(self):
        self.goodEpochString = '123456'
        self.badEpochString = '123bad456'
        self.badIsoStringShort = '87-9-12'
        self.badIsoStringShortFormat = '09-12-1988'
        self.badIsoStringLong = '09-1988-12-T12:'
        self.goodIsoStringShort = '1977-09-12'
        self.goodIsoStringLong = '1988-09-12T12:09:12'
        self.nonsense = 'om9c02mcm0]    r20938'

    def test_bad_epoch_time_input(self):
        self.assertFalse(validate.epoch_time(self.badEpochString))

    def test_good_epoch_time_input(self):
        self.assertEqual(int(self.goodEpochString), 123456)

    def test_bad_iso_time_input(self):
        self.assertFalse(validate.iso_time(self.badIsoStringShort))
        self.assertFalse(validate.iso_time(self.badIsoStringLong))
        self.assertFalse(validate.iso_time(self.badIsoStringShortFormat))

    def test_good_iso_time_input(self):
        self.assertTrue(validate.iso_time(self.goodIsoStringShort))
        self.assertTrue(validate.iso_time(self.goodIsoStringLong))

    def test_catch_neither_format(self):
        with self.assertRaises(TypeError):
            validate.validate_time(self.nonsense)

    def test_bad_time_range(self):
        with self.assertRaises(TypeError):
            validate.validate_time_range(self.goodIsoStringLong, self.goodIsoStringShort)

    def test_bad_time_range_mixed_format(self):
        with self.assertRaises(TypeError):
            validate.validate_time_range(self.goodIsoStringLong, self.goodEpochString)

    def test_good_time_range(self):
        self.assertTrue(validate.validate_time_range(self.goodIsoStringShort, self.goodIsoStringLong))

    def test_good_time_range_mixed_format(self):
        self.assertTrue(validate.validate_time_range(self.goodEpochString, self.goodIsoStringLong))

if __name__ == '___main___':
    unittest.main()
